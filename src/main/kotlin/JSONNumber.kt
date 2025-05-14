data class JsonNumber(val value: Number) : JsonValue() {
    override fun toJsonString(): String = value.toString()
}