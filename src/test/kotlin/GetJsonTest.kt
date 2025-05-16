package getjson

import getjson.annotations.Mapping
import getjson.annotations.Param
import getjson.annotations.Path
import getjson.core.GetJson
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import model.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.TestInstance
import kotlin.test.Test
import kotlin.test.assertEquals

@Mapping("api")
class Controller {
    @Mapping("ints")
    fun demo(): List<Int> = listOf(1, 2, 3)

    @Mapping("pair")
    fun obj(): Pair<String, String> = "um" to "dois"

    @Mapping("path/{pathvar}")
    fun path(@Path pathvar: String): String = "$pathvar!"

    @Mapping("args")
    fun args(@Param n: Int, @Param text: String): Map<String, String> =
        mapOf(text to text.repeat(n))
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetJsonTest {
    private val client = OkHttpClient()
    private val app = GetJson(Controller::class)
    private var port: Int = 8080

    @BeforeAll fun start() = app.start(port)
    @AfterAll fun stop() = app.stop()

    private fun get(path: String): String {
        val request = Request.Builder().url("http://localhost:8080$path").build()
        client.newCall(request).execute().use { return it.body!!.string() }
    }

    @Test
    fun testInts() {
        val expected = JsonInfer.from(listOf(1, 2, 3)).toJsonString()
        assertEquals(expected, get("/api/ints"))
    }

    @Test
    fun testPair() {
        val expected = JsonInfer.from(mapOf("first" to "um", "second" to "dois")).toJsonString()
        assertEquals(expected, get("/api/pair"))
    }

    @Test
    fun testPath() {
        assertEquals(JsonString("a!").toJsonString(), get("/api/path/a"))
        assertEquals(JsonString("b!").toJsonString(), get("/api/path/b"))
    }

    @Test
    fun testArgs() {
        val expected = JsonInfer.from(mapOf("PA" to "PAPAPA")).toJsonString()
        assertEquals(expected, get("/api/args?n=3&text=PA"))
    }
}
