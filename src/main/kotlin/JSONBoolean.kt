/**
 * A JsonValue of the Boolean type.
 *
 * @property value the value of this JsonBoolean.
 * @constructor creates a JsonBoolean with a value.
 */
data class JsonBoolean(val value: Boolean) : JsonValue() {
    /**
     * Serializes the JsonBoolean object to a string.
     * @return the JsonBoolean as a string.
     */
    override fun toJsonString(): String = value.toString()
}