import model.*
import org.junit.jupiter.api.Test
import kotlin.test.*

class JsonArrayTest {

    @Test
    fun `test empty array`() {
        val arr = JsonArray()
        assertEquals("[]", arr.toJsonString())
    }

    @Test
    fun `test array with elements`() {
        val arr = JsonArray(listOf(
            JsonString("a"),
            JsonNumber(1)
        ))
        assertEquals("""["a", 1]""", arr.toJsonString())
    }

    @Test
    fun `test array filtering`() {
        val arr = JsonArray(listOf(
            JsonNumber(1),
            JsonNumber(2)
        ))
        val filtered = arr.filter { it is JsonNumber && it.value.toInt() > 1 }
        assertEquals("[2]", filtered.toJsonString())
    }

    @Test
    fun `test array mapping`() {
        val arr = JsonArray(listOf(JsonNumber(1)))
        val mapped = arr.map { JsonString(it.toJsonString()) }
        assertEquals("""["1"]""", mapped.toJsonString())
    }
}