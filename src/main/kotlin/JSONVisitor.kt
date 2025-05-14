interface JsonVisitor<T> {
    fun visit(value: JsonValue): T
    fun visit(string: JsonString): T
    fun visit(number: JsonNumber): T
    fun visit(boolean: JsonBoolean): T
    fun visit(nullValue: JsonNull): T
    fun visit(array: JsonArray): T
    fun visit(obj: JsonObject): T
}

// Validations
class JsonValidationVisitor : JsonVisitor<Boolean> {
    private val visitedObjects = mutableSetOf<JsonObject>()

    override fun visit(value: JsonValue): Boolean = when (value) {
        is JsonString, is JsonNumber, is JsonBoolean, is JsonNull -> true
        is JsonArray -> visit(value) // Use the specific visit method for JsonArray
        is JsonObject -> visit(value) // Use the specific visit method for JsonObject
    }

    override fun visit(string: JsonString): Boolean = true

    override fun visit(number: JsonNumber): Boolean = true

    override fun visit(boolean: JsonBoolean): Boolean = true

    override fun visit(nullValue: JsonNull): Boolean = true

    override fun visit(array: JsonArray): Boolean {
        return array.values.all { visit(it) }
    }

    override fun visit(obj: JsonObject): Boolean {
        if (obj in visitedObjects) return false // Detect cycles
        visitedObjects.add(obj)
        var valid = true
        val uniqueKeys = mutableSetOf<String>()
        for ((key, value) in obj.properties) {
            if (key.isBlank() || !uniqueKeys.add(key)) {
                valid = false
                break
            }
            valid = valid && visit(value)
        }
        return valid
    }

    fun isValid(jsonValue: JsonValue): Boolean {
        visitedObjects.clear()
        return visit(jsonValue)
    }
}

class JsonTypeCheckVisitor : JsonVisitor<Boolean> {
    override fun visit(value: JsonValue): Boolean = when (value) {
        is JsonString, is JsonNumber, is JsonBoolean, is JsonNull -> true
        is JsonArray -> visit(value)
        is JsonObject -> visit(value)
    }

    override fun visit(string: JsonString): Boolean = true
    override fun visit(number: JsonNumber): Boolean = true
    override fun visit(boolean: JsonBoolean): Boolean = true
    override fun visit(nullValue: JsonNull): Boolean = true

    override fun visit(array: JsonArray): Boolean {
        if (array.values.isEmpty()) return true // Empty array is considered valid
        val firstType = array.values.firstOrNull()?.let { it::class } ?: return true //handle empty
        return array.values.all { it::class == firstType && it !is JsonNull }
    }

    override fun visit(obj: JsonObject): Boolean {
        return obj.properties.values.all { visit(it) }
    }

    fun checkArray(jsonArray: JsonArray): Boolean {
        return visit(jsonArray)
    }
}
