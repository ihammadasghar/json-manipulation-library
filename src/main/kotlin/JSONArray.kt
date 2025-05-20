/**
 * A JsonValue of the Array type.
 *
 * @property values the values of this JsonArray.
 * @constructor creates a JsonArray with values.
 */
data class JsonArray(val values: List<JsonValue>) : JsonValue() {
    /**
     * Serializes the JsonArray object to a string.
     * @return the JsonArray as a string.
     */
    override fun toJsonString(): String =
        values.joinToString(",", "[", "]") { it.toJsonString() }

    /**
     * Applies a [transform] to all JsonValues of the JsonArray.
     * @return a new JsonArray with the transformed values.
     */
    fun map(transform: (JsonValue) -> JsonValue): JsonArray {
        return JsonArray(values.map(transform))
    }
}

/**
 * Uses a [predicate] to filter the JsonValues of a JsonArray.
 * @return a new JsonArray with the filtered values.
 */
fun JsonArray.filterArray(predicate: (JsonValue) -> Boolean): JsonArray {
    val filteredValues = values.filter(predicate)
    return JsonArray(filteredValues)
}