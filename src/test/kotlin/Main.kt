import getjson.Controller
import getjson.GetJson

fun main() {
    val app = GetJson(Controller::class)
    val port = 8080
    app.start(port)
    println("Servidor disponível em http://localhost:$port")
}