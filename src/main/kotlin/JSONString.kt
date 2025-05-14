data class JsonString(val value: String) : JsonValue() {
    override fun toJsonString(): String = "\"${escapeString(value)}\""
}