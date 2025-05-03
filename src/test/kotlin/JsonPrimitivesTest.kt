import model.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class JsonPrimitivesTest {

    @Test
    fun `test JsonString`() {
        val str = JsonString("hello")
        assertEquals("\"hello\"", str.toJsonString())
    }

    @Test
    fun `test JsonNumber`() {
        val num = JsonNumber(42)
        assertEquals("42", num.toJsonString())
    }

    @Test
    fun `test JsonBoolean`() {
        val boolTrue = JsonBoolean(true)
        assertEquals("true", boolTrue.toJsonString())

        val boolFalse = JsonBoolean(false)
        assertEquals("false", boolFalse.toJsonString())
    }

    @Test
    fun `test JsonNull`() {
        assertEquals("null", JsonNull.toJsonString())
    }
}