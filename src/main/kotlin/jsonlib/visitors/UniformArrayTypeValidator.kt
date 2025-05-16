package jsonlib.visitors

import jsonlib.model.*

/**
 * Validates that all arrays contain elements of the same type (ignoring nulls)
 */
class UniformArrayTypeValidator : JsonVisitor {
    private var hasMixedTypes = false

    fun validate(root: JsonValue): Boolean {
        hasMixedTypes = false
        root.accept(this)
        return !hasMixedTypes
    }

    override fun visit(arr: JsonArray) {
        val nonNullElements = arr.elements.filter { it !is JsonNull }
        if (nonNullElements.map { it::class }.toSet().size > 1) {
            hasMixedTypes = true
        }
        arr.elements.forEach { it.accept(this) }
    }

    override fun visit(obj: JsonObject) = obj.properties.values.forEach { it.accept(this) }
    override fun visit(str: JsonString) = Unit
    override fun visit(num: JsonNumber) = Unit
    override fun visit(bool: JsonBoolean) = Unit
    override fun visit(nul: JsonNull) = Unit
}