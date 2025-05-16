import model.*
import visitors.*
import org.junit.jupiter.api.Test
import kotlin.test.*

class JsonVisitorsTest {

    // ==========================
    // ObjectValidator Tests
    // ==========================

    @Test
    fun `ObjectValidator should detect empty keys in JSON object`() {
        val json = Json.obj {
            this[""] = JsonString("invalid") // Empty key
            this["valid"] = JsonString("ok")
        }

        val errors = ObjectValidator().validate(json)
        assertTrue(errors.any { it.contains("Empty key") }, "Should detect empty key")
    }

    @Test
    fun `ObjectValidator should detect null values in JSON object`() {
        val json = Json.obj {
            this["name"] = JsonString("test")
            this["optional"] = JsonNull // Null value
        }

        val errors = ObjectValidator().validate(json)
        assertTrue(errors.any { it.contains("Null value") }, "Should detect null values")
    }

    @Test
    fun `ObjectValidator should pass with valid JSON object`() {
        val json = Json.obj {
            this["name"] = JsonString("test")
            this["count"] = JsonNumber(42)
        }

        val errors = ObjectValidator().validate(json)
        assertTrue(errors.isEmpty(), "Should pass with valid object")
    }

    @Test
    fun `ObjectValidator should detect empty keys in nested objects`() {
        val json = Json.obj {
            this["nested"] = Json.obj {
                this[""] = JsonString("invalid") // Empty key in nested object
            }
        }

        val errors = ObjectValidator().validate(json)
        assertTrue(errors.any { it.contains("Empty key") }, "Should detect empty key in nested object")
    }

    @Test
    fun `ObjectValidator should detect null values in nested objects`() {
        val json = Json.obj {
            this["nested"] = Json.obj {
                this["value"] = JsonNull // Null value in nested object
            }
        }

        val errors = ObjectValidator().validate(json)
        assertTrue(errors.any { it.contains("Null value") }, "Should detect null value in nested object")
    }

    // ==================================
    // UniformArrayTypeValidator Tests
    // ==================================

    @Test
    fun `UniformArrayTypeValidator should detect mixed types in array`() {
        val json = Json.arr {
            add(JsonString("text"))
            add(JsonNumber(42)) // Different type
        }

        assertFalse(UniformArrayTypeValidator().validate(json), "Should detect mixed types")
    }

    @Test
    fun `UniformArrayTypeValidator should ignore null values when checking types`() {
        val json = Json.arr {
            add(JsonString("text"))
            add(JsonNull) // Should be ignored
            add(JsonString("another"))
        }

        assertTrue(UniformArrayTypeValidator().validate(json), "Should ignore null values")
    }

    @Test
    fun `UniformArrayTypeValidator should pass with uniform types in array`() {
        val json = Json.arr {
            add(JsonNumber(1))
            add(JsonNumber(2))
        }

        assertTrue(UniformArrayTypeValidator().validate(json), "Should pass with uniform types")
    }

    @Test
    fun `UniformArrayTypeValidator should handle empty arrays`() {
        assertTrue(UniformArrayTypeValidator().validate(Json.arr {}), "Should pass with empty array")
    }

    @Test
    fun `UniformArrayTypeValidator should detect mixed types in nested arrays`() {
        val json = Json.obj {
            this["items"] = Json.arr {
                add(Json.arr {
                    add(JsonNumber(1))
                    add(JsonString("text")) // Mixed types in nested array
                })
            }
        }

        assertFalse(UniformArrayTypeValidator().validate(json), "Should detect mixed types in nested arrays")
    }
}