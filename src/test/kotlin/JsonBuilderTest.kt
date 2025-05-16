import jsonlib.Json
import jsonlib.model.JsonNumber
import jsonlib.model.JsonString
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class JsonBuilderTest {

    @Test
    fun `test build simple object`() {
        val obj = Json.obj {
            this["test"] = JsonString("value")
        }
        assertEquals("""{"test":"value"}""", obj.toJsonString())
    }

    @Test
    fun `test build nested objects`() {
        val obj = Json.obj {
            this["nested"] = Json.obj {
                this["num"] = JsonNumber(42)
            }
        }
        assertEquals("""{"nested":{"num":42}}""", obj.toJsonString())
    }

    @Test
    fun `test build array`() {
        val arr = Json.arr {
            add(JsonString("item"))
        }
        assertEquals("""["item"]""", arr.toJsonString())
    }
}