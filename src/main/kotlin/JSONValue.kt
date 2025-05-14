// Base class for all JSON type values
sealed class JsonValue {
    abstract fun toJsonString(): String
}