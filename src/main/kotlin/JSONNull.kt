/**
 * Object that represents the JSON Null value type.
 */
object JsonNull : JsonValue() {
    /**
     * Serializes the JsonNull object to a string.
     * @return the JsonNull object as a string.
     */
    override fun toJsonString(): String = "null"
}