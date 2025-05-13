package model
import visitors.*

/**
 * Represents a JSON array (ordered collection of values).
 *
 * @property elements The list of values that make up this JSON array
 */
data class JsonArray(val elements: List<JsonValue> = emptyList()) : JsonValue {
    /**
     * Accepts a [JsonVisitor] and triggers visitation of this array.
     *
     * The visitor will:
     * 1. First visit this array
     * 2. Then recursively visit all its elements
     *
     * @param visitor The visitor implementation that will process this array
     */
    override fun accept(visitor: JsonVisitor) {
        visitor.visit(this)
        elements.forEach { it.accept(visitor) }
    }

    /**
     * Serializes this JSON array to its standard string representation.
     *
     * @return A valid JSON array string representation
     */
    override fun toJsonString(): String {
        return "[${elements.joinToString(",") { it.toJsonString() }}]"
    }

    /**
     * Creates a new JSON array containing only elements that match the predicate.
     *
     * @param predicate Function that takes an element and returns true to keep it
     * @return A new JsonArray with only the matching elements
     */
    fun filter(predicate: (JsonValue) -> Boolean): JsonArray {
        return JsonArray(elements.filter(predicate))
    }

    /**
     * Creates a new JSON array by transforming each element.
     *
     * @param transform Function that maps each element to a new JsonValue
     * @return A new JsonArray with transformed elements
     */
    fun map(transform: (JsonValue) -> JsonValue): JsonArray {
        return JsonArray(elements.map(transform))
    }

    /**
     * Gets the element at the specified index.
     *
     * @param index Zero-based element position
     * @return The JsonValue at this index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    operator fun get(index: Int): JsonValue = elements[index]

    /**
     * Returns the number of elements in the array.
     */
    val length: Int get() = elements.size
}