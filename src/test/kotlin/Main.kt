import getjson.Controller
import getjson.core.GetJson

//Função para testes manuais, observando os endpoints no browser
fun main() {
    val app = GetJson(Controller::class)
    app.start(8080)
    println("Servidor disponível em http://localhost:8080")
}