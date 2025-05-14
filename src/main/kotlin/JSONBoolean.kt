class JsonBoolean(val value: Boolean) : JsonValue() {
    override fun toJsonString(): String = value.toString()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonBoolean) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}