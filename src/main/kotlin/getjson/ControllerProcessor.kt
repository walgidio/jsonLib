package getjson.core

import getjson.annotations.Mapping
import getjson.annotations.Param
import getjson.annotations.Path
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation

/**
 * Scans controller classes for @Mapping annotations and prepares route definitions.
 */
class ControllerProcessor(private val controllers: List<KClass<*>>) {

    /** All routes discovered from the given controllers. */
    val routes: List<RouteDefinition> = processControllers()

    /**
     * Data class representing a registered route (endpoint).
     * @param pathSegments Parsed URL path pattern segments (static or variable).
     * @param controller Instance of the controller class.
     * @param function The handler function to invoke for this route.
     * @param paramInfos Parameter binding info for each function parameter.
     */
    data class RouteDefinition(
        val pathSegments: List<PathSegment>,
        val controller: Any,
        val function: KFunction<*>,
        val paramInfos: List<ParamInfo>
    ) {
        /**
         * Checks if the given request path matches this route's pattern.
         * If matched, returns a map of path variable names to values; otherwise null.
         */
        fun matchAndExtract(path: String): Map<String, String>? {
            // Split the incoming path and compare against pattern segments
            val reqSegments = if (path.isEmpty()) emptyList() else path.split('/')
            if (reqSegments.size != pathSegments.size) return null  // segment count must match
            val pathVars = mutableMapOf<String, String>()
            for ((index, segmentPattern) in pathSegments.withIndex()) {
                val segmentValue = reqSegments[index]
                when (segmentPattern) {
                    is PathSegment.Fixed -> {
                        if (segmentPattern.part != segmentValue) {
                            return null  // static segment mismatch
                        }
                    }
                    is PathSegment.Variable -> {
                        pathVars[segmentPattern.name] = segmentValue
                    }
                }
            }
            return pathVars
        }

        /**
         * Invokes the controller function with given path and query parameters.
         * Performs type conversion for parameters and returns the result of the call.
         */
        fun invoke(pathVars: Map<String, String>, queryParams: Map<String, String>): Any? {
            // Prepare arguments for the function call by converting strings to target types
            val args = mutableListOf<Any?>()
            for (paramInfo in paramInfos) {
                val rawValue = if (paramInfo.source == SourceType.PATH) {
                    pathVars[paramInfo.name]
                } else {
                    queryParams[paramInfo.name]
                }
                if (rawValue == null) {
                    throw IllegalArgumentException("Missing required parameter: ${paramInfo.name}")
                }
                args.add(convertValue(rawValue, paramInfo.type))
            }
            // Invoke the function on the controller instance with the converted arguments
            return function.call(controller, *args.toTypedArray())
        }
    }

    /** Represents a segment of a route path (either fixed text or a variable placeholder). */
    sealed class PathSegment {
        data class Fixed(val part: String) : PathSegment()
        data class Variable(val name: String) : PathSegment()
    }

    /** Parameter binding information for a controller function parameter. */
    data class ParamInfo(val name: String, val type: KClass<*>, val source: SourceType)

    /** Source of parameter value (URL path vs query string). */
    enum class SourceType { PATH, QUERY }

    // Processes all given controllers and builds the list of RouteDefinition.
    private fun processControllers(): List<RouteDefinition> {
        val routes = mutableListOf<RouteDefinition>()
        for (controllerClass in controllers) {
            // Determine base path from class @Mapping (if present)
            val classMapping = controllerClass.findAnnotation<Mapping>()?.value ?: ""
            // Instantiate the controller (requires a no-arg constructor or default values)
            val controllerInstance = controllerClass.createInstance()

            // Reflect over each function in the controller
            for (func in controllerClass.declaredMemberFunctions) {
                val mappingAnn = func.findAnnotation<Mapping>() ?: continue  // only consider annotated functions
                val methodPath = mappingAnn.value
                // Combine class and method paths (trim to avoid duplicate slashes)
                val base = classMapping.trim('/')
                val subPath = methodPath.trim('/')
                var fullPath = if (base.isNotEmpty()) "$base/$subPath" else subPath
                fullPath = fullPath.trim('/')  // ensure no trailing slash

                // Parse the full path pattern into segments
                val segments = if (fullPath.isEmpty()) emptyList() else fullPath.split('/')
                val pathSegments = segments.map { segment ->
                    if (segment.startsWith("{") && segment.endsWith("}")) {
                        // Dynamic path variable segment (e.g., {id})
                        val varName = segment.substring(1, segment.length - 1)
                        PathSegment.Variable(varName)
                    } else {
                        // Static path segment
                        PathSegment.Fixed(segment)
                    }
                }

                // Build parameter info for this function
                val paramInfos = mutableListOf<ParamInfo>()
                for (param in func.parameters) {
                    if (param.kind != KParameter.Kind.VALUE) continue  // skip instance or extension receiver
                    val paramName = param.name ?: continue
                    val isPathParam = param.findAnnotation<Path>() != null
                    val isQueryParam = param.findAnnotation<Param>() != null
                    if (!isPathParam && !isQueryParam) {
                        throw IllegalStateException("Parameter '$paramName' of function '${func.name}' is not annotated with @Path or @Param")
                    }
                    // Determine the parameter's expected type class
                    val paramTypeClass = (param.type.classifier as? KClass<*>) ?: throw IllegalStateException("Unknown parameter type for $paramName")
                    if (isPathParam) {
                        paramInfos.add(ParamInfo(paramName, paramTypeClass, SourceType.PATH))
                    } else if (isQueryParam) {
                        paramInfos.add(ParamInfo(paramName, paramTypeClass, SourceType.QUERY))
                    }
                }

                // Validate that all path placeholders have corresponding @Path parameters and vice versa
                val placeholderNames = pathSegments.filterIsInstance<PathSegment.Variable>().map { it.name }.toSet()
                val pathParamNames = paramInfos.filter { it.source == SourceType.PATH }.map { it.name }.toSet()
                if (placeholderNames != pathParamNames) {
                    throw IllegalStateException("Path placeholders $placeholderNames do not match @Path params $pathParamNames in function '${func.name}'")
                }

                // Create and register the route definition
                routes.add(RouteDefinition(pathSegments, controllerInstance, func, paramInfos))
            }
        }
        return routes
    }
}

/**
 * Helper function to convert a string value to a target Kotlin type.
 * Supports Int, Double, Boolean, String, and Enums (throws IllegalArgumentException on failure).
 */
private fun convertValue(valueStr: String, targetType: KClass<*>): Any {
    return when (targetType) {
        String::class -> valueStr
        Int::class, Integer::class -> valueStr.toIntOrNull()
            ?: throw IllegalArgumentException("Invalid integer value: '$valueStr'")
        Double::class -> valueStr.toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid double value: '$valueStr'")
        Boolean::class -> when (valueStr.lowercase()) {
            "true" -> true
            "false" -> false
            else -> throw IllegalArgumentException("Invalid boolean value: '$valueStr'")
        }
        else -> {
            // Support enumeration types
            if (targetType.java.isEnum) {
                // Use Java Enum.valueOf to get the enum constant by name
                return java.lang.Enum.valueOf(targetType.java as Class<out Enum<*>>, valueStr)
            }
            throw IllegalArgumentException("Unsupported parameter type: $targetType")
        }
    }
}
