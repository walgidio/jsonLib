import getjson.controllers.Controller
import getjson.core.GetJson
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.*
import java.util.concurrent.Executors

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetJsonTest {

    private val port = 8081
    private val client = OkHttpClient()

    @BeforeAll
    fun setup() {
        Executors.newSingleThreadExecutor().submit {
            GetJson(Controller::class).start(port)
        }
        Thread.sleep(1000)
    }

    private fun get(url: String): String {
        val request = Request.Builder()
            .url("http://localhost:$port$url")
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string() ?: throw Exception("Sem resposta")
    }

    @Test
    fun testInts() {
        val response = get("/api/ints")
        Assertions.assertEquals("[1,2,3]", response)
    }

    @Test
    fun testPair() {
        val response = get("/api/pair")
        Assertions.assertEquals("""{"first":"um","second":"dois"}""", response)
    }

    @Test
    fun testPath() {
        val response = get("/api/path/ola")
        Assertions.assertEquals(""""ola!"""", response)
    }

    @Test
    fun testArgs() {
        val response = get("/api/args?n=3&text=PA")
        Assertions.assertEquals("""{"PA":"PAPAPA"}""", response)
    }
}
