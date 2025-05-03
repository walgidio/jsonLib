package model
import visitors.*

/**
 * Represents a JSON object (collection of key-value pairs).
 *
 * @property properties The map of key-value pairs that make up this JSON object
 */
data class JsonObject(val properties: Map<String, JsonValue> = emptyMap()) : JsonValue {
    /**
     * Accepts a [JsonVisitor] and triggers visitation of this object.
     *
     * The visitor will first visit this object, then recursively visit all its properties.
     *
     * @param visitor The visitor implementation that will process this object
     */
    override fun accept(visitor: JsonVisitor) {
        visitor.visit(this)
        properties.values.forEach { it.accept(visitor) }
    }

    /**
     * Serializes this JSON object to its standard string representation.
     *
     * @return A valid JSON object string representation
     */
    override fun toJsonString(): String {
        val props = properties.entries.joinToString(",") {
            """"${it.key}":${it.value.toJsonString()}"""
        }
        return "{$props}"
    }

    /**
     * Creates a new JSON object containing only properties that match the given predicate.
     *
     * @param predicate Function that takes a key-value pair and returns true to keep it
     * @return A new JsonObject with only the matching properties
     */
    fun filter(predicate: (Map.Entry<String, JsonValue>) -> Boolean): JsonObject {
        return JsonObject(properties.filter(predicate))
    }

    /**
     * Gets the value associated with the given key.
     *
     * @param key The property name to look up
     * @return The JsonValue associated with the key, or null if not found
     */
    operator fun get(key: String): JsonValue? = properties[key]
}