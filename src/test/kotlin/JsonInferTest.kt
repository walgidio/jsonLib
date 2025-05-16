import org.junit.jupiter.api.Test
import kotlin.test.*
import model.*

// Test Classes
enum class TestEnum { A, B }

data class SimpleData(val name: String, val age: Int, val height: Int)

data class NestedData(val inner: SimpleData)

data class NullableData(val a: String?, val b: Int?)

enum class EvalType { TEST, PROJECT, EXAM }
data class EvalItem(
    val name: String,
    val percentage: Double,
    val mandatory: Boolean,
    val type: EvalType?
)
data class Course(
    val name: String,
    val credits: Int,
    val evaluation: List<EvalItem>
)

class JsonInferTest {

    @Test
    fun `should convert String to JsonString`() {
        val result = JsonInfer.from("test")
        assertTrue(result is JsonString)
        assertEquals("\"test\"", result.toJsonString())
    }

    @Test
    fun `should convert Int to JsonNumber`() {
        val result = JsonInfer.from(42)
        assertTrue(result is JsonNumber)
        assertEquals("42", result.toJsonString())
    }

    @Test
    fun `should convert Boolean to JsonBoolean`() {
        val result = JsonInfer.from(true)
        assertTrue(result is JsonBoolean)
        assertEquals("true", result.toJsonString())
    }

    @Test
    fun `should convert null to JsonNull`() {
        val result = JsonInfer.from(null)
        assertTrue(result is JsonNull)
        assertEquals("null", result.toJsonString())
    }

    @Test
    fun `should convert List to JsonArray`() {
        val result = JsonInfer.from(listOf(1, "two", true))
        assertTrue(result is JsonArray)
        assertEquals("[1,\"two\",true]", result.toJsonString())
    }

    @Test
    fun `should convert Array to JsonArray`() {
        val result = JsonInfer.from(arrayOf(1, 2, 3))
        assertTrue(result is JsonArray)
        assertEquals("[1,2,3]", result.toJsonString())
    }

    @Test
    fun `should handle empty collections`() {
        val result = JsonInfer.from(emptyList<Any>())
        assertTrue(result is JsonArray)
        assertEquals("[]", result.toJsonString())
    }

    @Test
    fun `should convert String-key Map to JsonObject`() {
        val result = JsonInfer.from(mapOf("a" to 1, "b" to 2))
        assertTrue(result is JsonObject)
        assertEquals("""{"a":1,"b":2}""", result.toJsonString())
    }

    @Test
    fun `should convert non-String-key Map to JsonArray`() {
        val result = JsonInfer.from(mapOf(1 to "a", 2 to "b"))
        assertTrue(result is JsonArray)
        assertEquals("""[{"first":1,"second":"a"},{"first":2,"second":"b"}]""",
            result.toJsonString())
    }

    @Test
    fun `should convert data class to JsonObject`() {
        val result = JsonInfer.from(SimpleData("Alice", 30, 200))
        assertTrue(result is JsonObject)

        val jsonString = result.toJsonString()
        assertEquals("""{"name":"Alice","age":30,"height":200}""", jsonString)
    }

    @Test
    fun `should handle nested data classes`() {
        val result = JsonInfer.from(NestedData(SimpleData("Bob", 25, 100)))
        val expected = """{"inner":{"name":"Bob","age":25,"height":100}}"""
        assertEquals(expected, result.toJsonString())
    }

    @Test
    fun `should convert enum to JsonString`() {
        val result = JsonInfer.from(TestEnum.A)
        assertTrue(result is JsonString)
        assertEquals("\"A\"", result.toJsonString())
    }

    @Test
    fun `should handle nullable fields in data classes`() {
        val result = JsonInfer.from(NullableData(null, 42))
        assertEquals("""{"a":null,"b":42}""", result.toJsonString())
    }

    @Test
    fun `should throw IllegalArgumentException for non-data classes`() {
        class RegularClass(val a: String)
        assertFailsWith<IllegalArgumentException> {
            JsonInfer.from(RegularClass("test"))
        }
    }

    @Test
    fun `should convert example course structure correctly`() {
        val course = Course(
            name = "PA",
            credits = 6,
            evaluation = listOf(
                EvalItem("quizzes", 0.2, false, null),
                EvalItem("project", 0.8, true, EvalType.PROJECT)
            )
        )

        val result = JsonInfer.from(course).toJsonString()

        // Verifica cada componente separadamente
        assertTrue(result.contains(""""name":"PA""""))
        assertTrue(result.contains(""""credits":6"""))

        // Verifica os itens de avaliação
        assertTrue(result.contains(""""name":"quizzes""""))
        assertTrue(result.contains(""""percentage":0.2"""))
        assertTrue(result.contains(""""mandatory":false"""))
        assertTrue(result.contains(""""type":null"""))

        assertTrue(result.contains(""""name":"project""""))
        assertTrue(result.contains(""""percentage":0.8"""))
        assertTrue(result.contains(""""mandatory":true"""))
        assertTrue(result.contains(""""type":"PROJECT""""))

        // Verifica a estrutura básica
        assertTrue(result.startsWith("{"))
        assertTrue(result.endsWith("}"))
        assertTrue(result.contains(""","evaluation":["""))
    }
}