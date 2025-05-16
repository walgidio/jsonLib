import jsonlib.model.JsonNumber
import jsonlib.model.JsonObject
import jsonlib.model.JsonString
import org.junit.jupiter.api.Test
import kotlin.test.*

class JsonObjectTest {

    @Test
    fun `test empty object`() {
        val obj = JsonObject()
        assertEquals("{}", obj.toJsonString())
    }

    @Test
    fun `test object with properties`() {
        val obj = JsonObject(mapOf(
            "name" to JsonString("Ana"),
            "age" to JsonNumber(30)
        ))
        assertEquals("""{"name":"Ana","age":30}""", obj.toJsonString())
    }

    @Test
    fun `test object filtering`() {
        val obj = JsonObject(mapOf(
            "a" to JsonNumber(1),
            "b" to JsonNumber(2)
        ))
        val filtered = obj.filter { (k, _) -> k == "a" }
        assertEquals("""{"a":1}""", filtered.toJsonString())
    }
}