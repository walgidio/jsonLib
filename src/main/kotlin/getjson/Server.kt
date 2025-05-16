package getjson.core

import getjson.core.ControllerProcessor.RouteDefinition
import getjson.http.RequestParser
import getjson.http.ResponseBuilder
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import kotlin.concurrent.thread

class Server(private val routes: List<RouteDefinition>) {
    private lateinit var serverSocket: ServerSocket
    @Volatile private var running: Boolean = false

    fun start(port: Int) {
        serverSocket = ServerSocket(port)
        running = true
        thread(start = true) { acceptLoop() }
    }

    fun stop() {
        running = false
        try {
            serverSocket.close()
        } catch (_: Exception) {}
    }

    private fun acceptLoop() {
        try {
            while (running) {
                val clientSocket = serverSocket.accept()
                thread(start = true) { handleClient(clientSocket) }
            }
        } catch (e: SocketException) {}
    }

    private fun handleClient(socket: Socket) {
        socket.use { client ->
            val input = client.getInputStream()
            val output = client.getOutputStream()
            val request = RequestParser.parse(input)
            if (request == null) {
                val resp = ResponseBuilder.badRequest("Malformed Request")
                output.write(resp.toByteArray(Charsets.UTF_8))
                return
            }

            val match = findRoute(request.path)
            if (match == null) {
                val resp = ResponseBuilder.notFound()
                output.write(resp.toByteArray(Charsets.UTF_8))
            } else {
                val (route, pathVars) = match
                try {
                    val result = route.invoke(pathVars, request.queryParams)
                    val jsonValue = JsonInfer.from(result)
                    val jsonString = jsonValue.toJsonString()
                    val resp = ResponseBuilder.ok(jsonString)
                    output.write(resp.toByteArray(Charsets.UTF_8))
                } catch (e: Exception) {
                    val errorMsg = e.message ?: "Bad Request"
                    val resp = ResponseBuilder.badRequest(errorMsg)
                    output.write(resp.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    private fun findRoute(path: String): Pair<RouteDefinition, Map<String, String>>? {
        for (route in routes) {
            val vars = route.matchAndExtract(path)
            if (vars != null) {
                return route to vars
            }
        }
        return null
    }
}