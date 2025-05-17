package getjson.core

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import getjson.annotations.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import java.net.InetSocketAddress
import java.util.concurrent.Executors

/**
 * A lightweight HTTP server framework for handling GET requests with JSON responses.
 *
 * This class provides a simple way to create RESTful endpoints using Kotlin data classes
 * and functions, with automatic JSON serialization of response objects.
 *
 * Features:
 * - Path variable support via [Path] annotation
 * - Query parameter support via [Param] annotation
 * - Automatic route registration
 * - Built-in JSON serialization
 * - Thread-safe request handling
 *
 * @property routes List of registered routes with their metadata
 * @constructor Creates a new GetJson instance with the specified controllers
 * @param controllers Vararg of KClass instances to scan for endpoints
 */

class GetJson(vararg controllers: KClass<*>) {

    /**
     * Represents a registered route with its path template and handler method.
     *
     * @property templateSegments The path segments split by '/'
     * @property pathParams List of path parameter names (extracted from {param} segments)
     * @property method The Kotlin function that handles this route
     * @property instance The controller instance containing the handler method
     */
    data class Route(
        val templateSegments: List<String>,
        val pathParams: List<String>,
        val method: KFunction<*>,
        val instance: Any
    )

    /**
     * Represents a matched route with extracted path parameter values.
     *
     * @property route The matched route definition
     * @property pathValues Map of path parameter names to their values
     */
    data class RouteMatch(
        val route: Route,
        val pathValues: Map<String, String>
    )

    private val routes = mutableListOf<Route>()

    /**
     * Initializes the routing table by scanning the provided controller classes for
     * functions annotated with [Mapping] and registering their paths.
     */
    init {
        for (controllerClass in controllers) {
            val baseMapping = controllerClass.findAnnotation<Mapping>()?.value ?: ""
            val instance = controllerClass.createInstance()

            for (function in controllerClass.memberFunctions) {
                val methodMapping = function.findAnnotation<Mapping>()?.value ?: continue
                val fullPath = normalizePath("$baseMapping/$methodMapping")

                val segments = fullPath.split("/").filter { it.isNotBlank() }
                val pathParams = segments.filter { it.startsWith("{") && it.endsWith("}") }
                    .map { it.removePrefix("{").removeSuffix("}") }

                routes.add(Route(segments, pathParams, function, instance))
            }
        }
    }

    /**
     * Starts the HTTP server on the specified port.
     *
     * @param port The TCP port to listen on
     * @throws IOException If the server cannot bind to the port
     */
    fun start(port: Int) {
        val server = HttpServer.create(InetSocketAddress(port), 0)

        server.createContext("/") { exchange ->
            handleRequest(exchange)
        }

        server.executor = Executors.newFixedThreadPool(4)
        server.start()
        println("GetJson running on http://localhost:$port/")
    }

    /**
     * Handles an incoming HTTP request, performs route matching, invokes the handler,
     * and sends the JSON response.
     *
     * @param exchange The incoming HTTP exchange object
     */
    private fun handleRequest(exchange: HttpExchange) {
        try {
            if (exchange.requestMethod != "GET") {
                respond(exchange, 405, """{"error":"Method Not Allowed"}""")
                return
            }

            val path = exchange.requestURI.path
            val pathSegments = path.split("/").filter { it.isNotBlank() }
            val queryParams = parseQueryParams(exchange.requestURI.rawQuery)

            val matched = matchRoute(pathSegments)

            if (matched == null) {
                respond(exchange, 404, """{"error":"Not Found"}""")
                return
            }

            val args = buildArguments(matched.route, matched.pathValues, queryParams)
            val result = invokeRoute(matched.route, args)

            val json = JsonInfer.from(result).toJsonString()
            respond(exchange, 200, json)

        } catch (e: Exception) {
            e.printStackTrace()
            respond(exchange, 500, """{"error":"Internal Server Error"}""")
        }
    }

    /**
     * Attempts to find a matching route for the given request path segments.
     *
     * @param pathSegments List of segments from the request path
     * @return A [RouteMatch] if a matching route is found, or null otherwise
     */
    private fun matchRoute(pathSegments: List<String>): RouteMatch? {
        for (route in routes) {
            if (pathSegments.size != route.templateSegments.size) continue

            val pathValues = mutableMapOf<String, String>()
            var match = true

            for ((actual, template) in pathSegments.zip(route.templateSegments)) {
                if (template.startsWith("{") && template.endsWith("}")) {
                    val name = template.removePrefix("{").removeSuffix("}")
                    pathValues[name] = actual
                } else if (actual != template) {
                    match = false
                    break
                }
            }

            if (match) {
                return RouteMatch(route, pathValues)
            }
        }
        return null
    }

    /**
     * Builds the list of arguments to be passed to the route handler, resolving
     * path and query parameters.
     *
     * @param route The matched route
     * @param pathValues Path variable values
     * @param queryParams Query parameter values
     * @return A list of resolved argument values
     */
    private fun buildArguments(
        route: Route,
        pathValues: Map<String, String>,
        queryParams: Map<String, String>
    ): List<Any?> {
        val args = mutableListOf<Any?>()

        for (param in route.method.parameters) {
            if (param.kind != KParameter.Kind.VALUE) continue

            val pathAnno = param.findAnnotation<Path>()
            val queryAnno = param.findAnnotation<Param>()

            val rawValue = when {
                pathAnno != null -> pathValues[pathAnno.name]
                queryAnno != null -> queryParams[queryAnno.name]
                else -> null
            }

            args.add(rawValue?.let { convertValue(it, param.type) })
        }

        return args
    }

    /**
     * Invokes the route handler method using reflection.
     *
     * @param route The route to invoke
     * @param args List of arguments to pass to the handler
     * @return The return value of the invoked method
     */
    private fun invokeRoute(route: Route, args: List<Any?>): Any? {
        return route.method.call(route.instance, *args.toTypedArray())
    }

    /**
     * Converts a raw string value to the expected Kotlin type.
     *
     * @param value The string value to convert
     * @param type The target Kotlin type
     * @return The converted value
     * @throws IllegalArgumentException If the type is not supported
     */
    private fun convertValue(value: String, type: KType): Any {
        return when (type.classifier) {
            Int::class -> value.toInt()
            Long::class -> value.toLong()
            Double::class -> value.toDouble()
            Boolean::class -> value.toBooleanStrictOrNull() ?: false
            String::class -> value
            else -> throw IllegalArgumentException("Not supported type: $type")
        }
    }

    /**
     * Parses query parameters from the URL string.
     *
     * @param query The raw query string
     * @return A map of query parameter names to values
     */
    private fun parseQueryParams(query: String?): Map<String, String> {
        if (query == null) return emptyMap()
        return query.split("&").mapNotNull {
            val parts = it.split("=")
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }

    /**
     * Normalizes a path by removing extra slashes and trimming trailing slashes.
     *
     * @param path The original path
     * @return A normalized path string
     */
    private fun normalizePath(path: String): String {
        var p = path.replace(Regex("/+"), "/")
        if (!p.startsWith("/")) p = "/$p"
        return p.trimEnd('/')
    }

    /**
     * Sends a JSON response with the specified status code and body.
     *
     * @param exchange The HTTP exchange object
     * @param code The HTTP status code
     * @param body The JSON response body
     */
    private fun respond(exchange: HttpExchange, code: Int, body: String) {
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(code, body.toByteArray().size.toLong())
        exchange.responseBody.use { it.write(body.toByteArray()) }
    }
}
