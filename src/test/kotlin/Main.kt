import getjson.Controller
import getjson.core.GetJson

//Função para testes manuais, observando os endpoints no browser
fun main() {
    val app = GetJson(Controller::class)
    val port = 8080
    app.start(port)
    println("Servidor disponível em http://localhost:$port")
}