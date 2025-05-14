class JsonArray(val values: List<JsonValue>) : JsonValue() {
    override fun toJsonString(): String =
        values.joinToString(",", "[", "]") { it.toJsonString() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonArray) return false
        return values == other.values
    }

    override fun hashCode(): Int {
        return values.hashCode()
    }

    fun map(transform: (JsonValue) -> JsonValue): JsonArray {
        return JsonArray(values.map(transform))
    }
}

fun JsonArray.filterArray(predicate: (JsonValue) -> Boolean): JsonArray {
    val filteredValues = values.filter(predicate)
    return JsonArray(filteredValues)
}
