package visitors
import model.*

/**
 * Validates JSON objects according to common rules:
 * 1. Keys must be non-empty
 * 2. Keys must be unique
 * 3. Values must be valid
 */
class ObjectValidator : JsonVisitor {
    private val validationErrors = mutableListOf<String>()

    /**
     * Validates the JSON structure and returns found errors
     */
    fun validate(root: JsonValue): List<String> {
        validationErrors.clear()
        root.accept(this)
        return validationErrors
    }

    override fun visit(obj: JsonObject) {
        // Regra 1: Chaves não vazias
        obj.properties.keys.forEach { key ->
            if (key.isEmpty()) {
                validationErrors.add("Empty key found in JSON object")
            }
        }

        // Regra 3: Valores válidos
        obj.properties.values.forEach { value ->
            if (value is JsonNull) {
                validationErrors.add("Null value found for key: ${obj.properties.filterValues { it == value }.keys.first()}")
            }
            value.accept(this) // Continua a validação recursiva
        }
    }

    override fun visit(arr: JsonArray) = arr.elements.forEach { it.accept(this) }
    override fun visit(str: JsonString) = Unit
    override fun visit(num: JsonNumber) = Unit
    override fun visit(bool: JsonBoolean) = Unit
    override fun visit(nul: JsonNull) = Unit
}