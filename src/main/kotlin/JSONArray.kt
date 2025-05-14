data class JsonArray(val values: List<JsonValue>) : JsonValue() {
    override fun toJsonString(): String =
        values.joinToString(",", "[", "]") { it.toJsonString() }

    fun map(transform: (JsonValue) -> JsonValue): JsonArray {
        return JsonArray(values.map(transform))
    }
}

fun JsonArray.filterArray(predicate: (JsonValue) -> Boolean): JsonArray {
    val filteredValues = values.filter(predicate)
    return JsonArray(filteredValues)
}