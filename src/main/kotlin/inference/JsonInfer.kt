import model.*
import visitors.*

import kotlin.reflect.full.memberProperties

/**
 * Converts Kotlin objects to JSON model instances using reflection.
 *
 * Supports the following Kotlin types:
 * - Primitives (String, Number, Boolean)
 * - Nullable types
 * - Collections (List, Set)
 * - Maps (with String keys)
 * - Data classes
 * - Enums
 * - Nested combinations of the above
 */
object JsonInfer {

    /**
     * Converts any Kotlin object to its JSON model equivalent.
     *
     * @param source The object to convert, can be null
     * @return JsonValue representing the converted object
     * @throws IllegalArgumentException if unsupported types are encountered
     */
    fun from(source: Any?): JsonValue {
        return when (source) {
            // Handle null values first
            null -> JsonNull

            // Primitive types
            is String -> JsonString(source)
            is Number -> JsonNumber(source)
            is Boolean -> JsonBoolean(source)

            // Collections
            is Collection<*> -> convertCollection(source)
            is Array<*> -> convertCollection(source.toList())
            is Map<*, *> -> convertMap(source)

            // Enums
            is Enum<*> -> JsonString(source.name)

            // Kotlin data classes
            else -> convertDataClass(source)
        }
    }

    /**
     * Converts collections to JsonArray.
     * @param collection The collection to convert
     */
    private fun convertCollection(collection: Collection<*>): JsonArray {
        return JsonArray(collection.map { from(it) })
    }

    /**
     * Converts maps to JsonObject (if keys are Strings) or JsonArray of entries.
     * @param map The map to convert
     */
    private fun convertMap(map: Map<*, *>): JsonValue {
        return if (map.keys.all { it is String }) {
            JsonObject(map.mapKeys { it.key as String }.mapValues { from(it.value) })
        } else {
            // Fallback for non-String key maps
            JsonArray(map.entries.map { from(it.toPair()) })
        }
    }

    /**
     * Converts data class instances to JsonObject using reflection.
     * @param obj The object to convert
     */
    private fun convertDataClass(obj: Any): JsonObject {
        require(obj::class.isData) { "Only data classes are supported for automatic conversion" }

        val properties = obj::class.memberProperties
            .associate { prop ->
                val value = prop.call(obj)
                prop.name to from(value)
            }
        return JsonObject(properties)
    }
}