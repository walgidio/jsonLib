# GetJson + JsonLib

**Mestrado em Engenharia Informática – ISCTE 2024/2025**  
**Realizado por:** Dário Cunha (105229) e Walgídio Santos (131797)

---

## Overview

Este projeto consiste na implementação de 2 partes principais:

- Uma **biblioteca JSON em Kotlin (jsonLib)**, que permite representar, manipular e serializar estruturas JSON inteiramente em memória.
- **GetJson** - Um framework HTTP que mapeia os métodos anotados dos controladores para endpoints GET e retorna os seus resultados como JSON, usando o JsonLib.

---

## Estrutura do Projeto

---

## Fase 1 — JsonLibrary (Modelo em Memória)

A primeira fase do projeto consiste no desenvolvimento de uma estrutura de dados que representa, em memória, qualquer valor JSON de forma segura e manipulável. A implementação define uma hierarquia de tipos que permite construir objetos e arrays JSON, aplicar transformações e exportar o conteúdo para texto em formato JSON.

### Dentro da biblioteca, possuimos 4 tipos principais:

- **JsonArray**: Representa um conjunto ordenado de valores JSON
- **JsonObject**: Representa um conjunto de pares Chave-valor ordenados (recorrendo ao uso do LinkedHashMap)
- **JsonPrimitives**: Contém os diversos valores primitivos (JsonBoolean, JsonNumber (Int, Double, Float), JsonString, JsonNull)
- **JsonValue**: Interface base para todos os tipos JSON. Define o comportamento comum e permite a aplicação do padrão Visitor para validações e operações genéricas.

É oferecido suporte à serialização para o formato JSON padrão (serialize()) de qualquer tipo da hierarquia

### Foram implementadas duas validações recorrendo ao padrão **Visitor**, que percorre recursivamente a estrutura JSON:**

#### ObjectValidator

Classe responsável por validar objetos JSON com base em regras comuns:

- **Chaves não podem ser vazias**
- **Valores null são considerados inválidos**
- **A validação é recursiva** (também percorre arrays ou objetos aninhados)

Método principal:  
```kotlin
fun validate(root: JsonValue): List<String>
```

Este método devolve uma lista de erros encontrados durante a validação.

#### UniformArrayTypeValidator

Classe dedicada a validar arrays, garantindo que todos os elementos (excluindo **null**) sejam do mesmo tipo.

 Método principal:  
```kotlin
fun validate(root: JsonValue): Boolean
```

Retorna `true` se todos os arrays forem homogéneos em termos de tipo; caso contrário, `false`.

### Exemplos de Uso:

#### Criação e Serialização de um JsonObject**

```kotlin
val obj = Json.obj {
    this["nome"] = JsonString("Alice")
    this["idade"] = JsonNumber(30)
    this["ativo"] = JsonBoolean(true)
}
println(obj.toJsonString())
```

Saída Esperada:
```json
{"nome":"Alice","idade":30,"ativo":true}
```

#### Validação de objetos com ObjectValidator e UniformArrayTypeValidator**

```kotlin
val validator = ObjectValidator()
val erros = validator.validate(obj)
println("Erros: $erros")

val homog = UniformArrayTypeValidator()
val valido = homog.validate(arr)
println("Array homogéneo? $valido")

```

#### Filtragem e Mapeamento de Arrays**
``` kotlin
val numeros = Json.arr {
    add(JsonNumber(1))
    add(JsonNumber(2))
    add(JsonNumber(3))
    add(JsonNumber(4))
}

// Filtrar apenas múltiplos de 3
val multiplosDeTres = numeros.filter {
    (it as JsonNumber).value.toInt() % 3 == 0
}
println(multiplosDeTres.toJsonString()) // [3]

// Quadrado dos números
val aoQuadrado = numeros.map {
    if (it is JsonNumber) JsonNumber(it.value.toInt() * it.value.toInt()) else it
}
println(aoQuadrado.toJsonString()) // [1, 4, 9, 16]
```



---

## Fase 2 — Json Inference

Esta fase adiciona suporte à **conversão automática de objetos Kotlin para estruturas JSON** da biblioteca. A funcionalidade é implementada em `JsonInfer`, que usa **reflexão** para transformar dados Kotlin em instâncias de `JsonValue`.


### O JsonInfer dá suporte a diversos tipos, nomeadamente:

- Tipos primitivos (`String`, `Number`, `Boolean`, `null`)
- Coleções (`List`, `Set`, `Array`)
- `Map<String, *>`
- `Enum`
- `data class` com propriedades compatíveis
- Combinações aninhadas (nested) dos vários tipos

### A reflexão é feita segundo a seguinte função from:

```kotlin
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
```

### Exemplos de Utilização

#### Conversão de data class simples

```kotlin
data class Livro(val titulo: String, val paginas: Int)

val json = JsonInfer.from(Livro("Dom Quixote", 863))
println(json.toJsonString())
// Saída:
// {"titulo":"Dom Quixote","paginas":863}

```

---

## Fase 3 —  GetJson HTTP Framework

Nesta fase foi desenvolvido o microframework `GetJson`, implementado na classe `getjson.core.GetJson`, que permite expor métodos de controladores Kotlin como endpoints HTTP GET com respostas automáticas em JSON.

### Funcionamento da Estrutura

Ao iniciar o servidor com `GetJson(Controller::class).start(port)`, ocorre o seguinte processo:

1. **Descoberta de Endpoints**
- A classe GetJson usa a reflexão para detetar métodos anotados com @Mapping e construir rotas a partir do path da classe e do método.
2. **Resolução de Parâmetros**
- Os Parâmetros do método anotado são mapeados de forma dinâmica:
  - `@Path("nome")` (de `getjson.annotations.Path`) liga variáveis da URL como `/rota/{valor}` ao argumento correspondente.
  - `@Param("nome")` (de `getjson.annotations.Param`) extrai parâmetros da query string como `?chave=valor`.

3. **Execução e Serialização**
- A função é invocada por reflexão com os argumentos resolvidos (`invokeRoute`).
- O valor de retorno é convertido automaticamente em JSON com `JsonInfer.from(...)` (implementado na Fase 2).
- A resposta JSON é enviada com `Content-Type: application/json`.
  A estrutura é gerida com threads através do executor interno (`Executors.newFixedThreadPool(4)`), permitindo o processamento de múltiplos pedidos HTTP (neste caso, 4) em simultâneo


---

### Exemplos de Utilização

#### Definição do controlador

```kotlin
@Mapping("api")
class Controller {

    @Mapping("ints")
    fun demo(): List<Int> = listOf(1, 2, 3)

    @Mapping("pair")
    fun obj(): Pair<String, String> = Pair("um", "dois")

    @Mapping("path/{pathvar}")
    fun path(@Path("pathvar") pathvar: String): String = pathvar + "!"

    @Mapping("args")
    fun args(@Param("n") n: Int, @Param("text") text: String): Map<String, String> =
        mapOf(text to text.repeat(n))
}
```
#### Inicialização do server

```kotlin
val app = GetJson(Controller::class)
val port = 8080
app.start(port)
```
**Em Relação a testes, estes foram efetuados usando JUnit e OkHttp, que verifica o comportamento completo do servidor:**

```kotlin
@Test
fun testArgs() {
    val response = get("/api/args?n=3&text=PA")
    Assertions.assertEquals("""{"PA":"PAPAPA"}""", response)
}

```




