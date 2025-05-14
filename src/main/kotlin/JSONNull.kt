object JsonNull : JsonValue() {
    override fun toJsonString(): String = "null"
}