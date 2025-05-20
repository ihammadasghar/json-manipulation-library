/**
 * Base class for all JSON type values.
 */
sealed class JsonValue {
    /**
     * Serializes the JsonValue object to a string.
     * @return the JsonValue as a string.
     */
    abstract fun toJsonString(): String
}