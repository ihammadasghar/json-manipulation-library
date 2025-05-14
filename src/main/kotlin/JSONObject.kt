data class JsonObject(val properties: Map<String, JsonValue>) : JsonValue() {
    override fun toJsonString(): String {
        val props = properties.entries.joinToString(",") { (key, value) ->
            "\"${escapeString(key)}\":${value.toJsonString()}"
        }
        return "{$props}"
    }
}

fun JsonObject.filterObject(predicate: (String, JsonValue) -> Boolean): JsonObject {
    val filteredProperties = properties.filter { (key, value) -> predicate(key, value) }
    return JsonObject(filteredProperties)
}
