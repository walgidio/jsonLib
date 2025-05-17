package getjson.controllers

import getjson.annotations.Mapping
import getjson.annotations.Param
import getjson.annotations.Path

@Mapping("api")
class Controller {

    @Mapping("ints")
    fun demo(): List<Int> = listOf(1, 2, 3)

    @Mapping("pair")
    fun obj(): Pair<String, String> = Pair("um", "dois")

    @Mapping("path/{pathvar}")
    fun path(
        @Path("pathvar") pathvar: String
    ): String = pathvar + "!"

    @Mapping("args")
    fun args(
        @Param("n") n: Int,
        @Param("text") text: String
    ): Map<String, String> = mapOf(text to text.repeat(n))
}
