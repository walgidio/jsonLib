package getjson

import getjson.controllers.Controller
import getjson.core.GetJson

fun main() {
    val app = GetJson(Controller::class)
    val port = 8080
    app.start(port)
    println("Servidor disponível em http://localhost:$port")
}