import model.*

/**
 * Helper para criar JSON de forma mais simples
 */
object Json {
    // Cria objeto JSON
    fun obj(block: MutableMap<String, JsonValue>.() -> Unit): JsonObject {
        val map = mutableMapOf<String, JsonValue>()
        map.block()
        return JsonObject(map)
    }

    // Cria array JSON
    fun arr(block: MutableList<JsonValue>.() -> Unit): JsonArray {
        val list = mutableListOf<JsonValue>()
        list.block()
        return JsonArray(list)
    }
}