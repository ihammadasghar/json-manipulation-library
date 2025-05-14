class JsonObject(val properties: Map<String, JsonValue>) : JsonValue() {
    override fun toJsonString(): String {
        val props = properties.entries.joinToString(",") { (key, value) ->
            "\"${escapeString(key)}\":${value.toJsonString()}"
        }
        return "{$props}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonObject) return false
        return properties == other.properties
    }

    override fun hashCode(): Int {
        return properties.hashCode()
    }
}

fun JsonObject.filterObject(predicate: (String, JsonValue) -> Boolean): JsonObject {
    val filteredProperties = properties.filter { (key, value) -> predicate(key, value) }
    return JsonObject(filteredProperties)
}
