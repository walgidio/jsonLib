package model
import visitors.*

/**
 * This sealed interface serves as the foundation for the JSON data model and enables:
 * - Type-safe handling of all JSON value types
 * - Visitor pattern implementation for traversing JSON structures
 * - Serialization to standard JSON string format
 */
sealed interface JsonValue {
    /**
     * Accepts a [JsonVisitor] to process this JSON value.
     *
     * This method implements the Visitor pattern, allowing external operations to be performed
     * on the JSON structure without modifying its classes.
     *
     * @param visitor The visitor implementation that will process this value
     */
    fun accept(visitor: JsonVisitor)

    /**
     * Serializes this JSON value to its standard string representation.
     *
     * The output must be valid JSON according to RFC 8259.
     * Formatting (whitespace, indentation) is not strictly required as long as the syntax is correct.
     *
     * @return A valid JSON string representation of this value
     */
    fun toJsonString(): String
}