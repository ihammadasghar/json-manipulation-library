/**
 * A JsonValue of the Number type.
 *
 * @property value the value of this JsonNumber.
 * @constructor creates a JsonNumber with a value.
 */
data class JsonNumber(val value: Number) : JsonValue() {
    /**
     * Serializes the JsonNumber object to a string.
     * @return the JsonNumber as a string.
     */
    override fun toJsonString(): String = value.toString()
}