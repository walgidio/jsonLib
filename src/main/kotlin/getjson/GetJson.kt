package getjson

import kotlin.reflect.KClass

/**
 * Main framework class. Registers controllers and starts the HTTP server.
 */
class GetJson(vararg controllers: KClass<*>) {
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
