/**
 * A JsonValue of the Object type.
 *
 * @property properties the properties of this JsonObject.
 * @constructor creates a JsonObject with properties as a map.
 */
data class JsonObject(val properties: Map<String, JsonValue>) : JsonValue() {
    /**
     * Serializes the JsonObject object to a string.
     * @return the JsonObject as a string.
     */
    override fun toJsonString(): String {
        val props = properties.entries.joinToString(",") { (key, value) ->
            "\"${escapeString(key)}\":${value.toJsonString()}"
        }
        return "{$props}"
    }
}

/**
 * Uses a [predicate] to filter the properties of a JsonObject.
 * @return a new JsonObject with the filtered properties.
 */
fun JsonObject.filterObject(predicate: (String, JsonValue) -> Boolean): JsonObject {
    val filteredProperties = properties.filter { (key, value) -> predicate(key, value) }
    return JsonObject(filteredProperties)
}
