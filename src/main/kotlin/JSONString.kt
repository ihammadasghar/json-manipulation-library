/**
 * A JsonValue of the String type.
 *
 * @property value the value of this JsonString.
 * @constructor creates a JsonString with a value.
 */
data class JsonString(val value: String) : JsonValue() {
    /**
     * Serializes the JsonString object to a string.
     * @return the JsonString as a string.
     */
    override fun toJsonString(): String = "\"${escapeString(value)}\""
}