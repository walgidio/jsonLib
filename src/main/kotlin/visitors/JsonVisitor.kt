package visitors
import model.*
/**
 * Visitor interface for processing JSON structures.
 *
 * Provides type-safe visit methods for each JSON value type.
 */
interface JsonVisitor {
    fun visit(obj: JsonObject)
    fun visit(arr: JsonArray)
    fun visit(str: JsonString)
    fun visit(num: JsonNumber)
    fun visit(bool: JsonBoolean)
    fun visit(nul: JsonNull)
}