object JsonNull : JsonValue() {
    override fun toJsonString(): String = "null"
    override fun equals(other: Any?): Boolean = other === JsonNull
    override fun hashCode(): Int = 0
}