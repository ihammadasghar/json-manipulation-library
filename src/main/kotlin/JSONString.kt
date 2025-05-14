class JsonString(val value: String) : JsonValue() {
    override fun toJsonString(): String = "\"${escapeString(value)}\""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonString) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}