import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Alters a [s] String to contain escape sequences as text.
 * @return a new String with the escape sequences represented as text.
 */
fun escapeString(s: String): String {
    return s.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
        .replace("\b", "\\b")
        .replace("\u000c", "\\f")
}

/**
 * Converts a non JSON [obj] into the proper type of JsonValue.
 * Most types of [obj] will be converted directly.
 * Data classes will have their properties converted and stored on a JsonObject.
 * @return a new JsonValue created from the conversion.
 */
fun toJsonValue(obj: Any?): JsonValue {
    return when (obj) {
        null -> JsonNull
        is Int, is Double -> JsonNumber(obj as Number)
        is Boolean -> JsonBoolean(obj)
        is String -> JsonString(obj)
        is List<*> -> JsonArray(obj.map { toJsonValue(it) })
        is Enum<*> -> JsonString(obj.name)
        is Map<*, *> -> {
            val map = obj
            val jsonProperties = map.mapKeys { it.key as String }.mapValues { toJsonValue(it.value) }
            JsonObject(jsonProperties)
        }
        else -> {
            val kClass = obj::class
            if (kClass.isData) {
                val properties = kClass.memberProperties
                val jsonProperties = properties.associate { prop ->
                    val value = (prop as KProperty1<Any, *>).get(obj)
                    prop.name to toJsonValue(value)
                }
                JsonObject(jsonProperties)
            } else {
                throw IllegalArgumentException("Unsupported type: ${kClass.qualifiedName}")
            }
        }
    }
}