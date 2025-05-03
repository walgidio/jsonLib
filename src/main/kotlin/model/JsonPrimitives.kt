package model
import model.visitors.*

/**
 * Represents a JSON string value.
 *
 * @property value The underlying string value
 *
 * JSON strings must:
 * - Be wrapped in double quotes
 * - Have special characters properly escaped
 * - Support Unicode characters
 */
data class JsonString(val value: String) : JsonValue {
    /**
     * Accepts a [JsonVisitor] for processing this string value.
     */
    override fun accept(visitor: JsonVisitor) = visitor.visit(this)

    /**
     * Serializes this string to valid JSON format.
     *
     * Handles escaping of:
     * - Quotes (\")
     * - Backslashes (\\)
     * - Control characters (\n, \t, etc.)
     * - Unicode characters (\uXXXX)
     */
    override fun toJsonString(): String {
        return buildString {
            append('"')
            value.forEach { char ->
                when (char) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> {
                        if (char < ' ') {
                            append("\\u%04X".format(char.code))
                        } else {
                            append(char)
                        }
                    }
                }
            }
            append('"')
        }
    }
}

/**
 * Represents a JSON number value.
 *
 * @property value The underlying numeric value
 *
 * Supports all numeric types that Kotlin's Number class handles:
 * - Int, Long, Short, Byte
 * - Double, Float
 * - BigDecimal, etc.
 */
data class JsonNumber(val value: Number) : JsonValue {
    /**
     * Accepts a [JsonVisitor] for processing this number value.
     */
    override fun accept(visitor: JsonVisitor) = visitor.visit(this)

    /**
     * Serializes this number to valid JSON format.
     *
     * Follows JSON number specifications:
     * - No leading/trailing decimal point
     * - No NaN or Infinity
     * - Scientific notation allowed
     */
    override fun toJsonString(): String {
        return when (value) {
            is Float, is Double -> {
                val str = value.toString()
                if (str.endsWith(".0")) str.dropLast(2) else str
            }
            else -> value.toString()
        }
    }
}

/**
 * Represents a JSON boolean value (true or false).
 *
 * @property value The underlying boolean value
 */
data class JsonBoolean(val value: Boolean) : JsonValue {
    /**
     * Accepts a [JsonVisitor] for processing this boolean value.
     */
    override fun accept(visitor: JsonVisitor) = visitor.visit(this)

    /**
     * Serializes this boolean to valid JSON format.
     *
     * Returns either "true" or "false" without quotes.
     */
    override fun toJsonString(): String = value.toString()
}

/**
 * Represents a JSON null value.
 *
 * Singleton object since there's only one null value in JSON.
 */
object JsonNull : JsonValue {
    /**
     * Accepts a [JsonVisitor] for processing this null value.
     */
    override fun accept(visitor: JsonVisitor) = visitor.visit(this)

    /**
     * Serializes this null to valid JSON format.
     *
     * Always returns "null" without quotes.
     */
    override fun toJsonString(): String = "null"
}