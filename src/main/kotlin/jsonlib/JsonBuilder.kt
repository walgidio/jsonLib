package jsonlib

import jsonlib.model.JsonArray
import jsonlib.model.JsonObject
import jsonlib.model.JsonValue

/**
 * Helper to create new JSON arrays and objects
 */
object Json {
    // Criar objeto JSON
    fun obj(block: MutableMap<String, JsonValue>.() -> Unit): JsonObject {
        val map = mutableMapOf<String, JsonValue>()
        map.block()
        return JsonObject(map)
    }

    // Criar array JSON
    fun arr(block: MutableList<JsonValue>.() -> Unit): JsonArray {
        val list = mutableListOf<JsonValue>()
        list.block()
        return JsonArray(list)
    }
}