package getjson.core

import kotlin.reflect.KClass

/**
 * Main framework class. Registers controllers and starts the HTTP server.
 */
class GetJson(vararg controllers: KClass<*>) {
    // Process controllers to obtain all route definitions
    private val routes = ControllerProcessor(controllers.toList()).routes
    private var server: Server? = null

    /**
     * Starts the HTTP server on the given port.
     */
    fun start(port: Int) {
        server = Server(routes)
        server?.start(port)
    }

    /**
     * Stops the HTTP server (if running).
     */
    fun stop() {
        server?.stop()
    }
}
