/**
 * Interface used to visit different types of JsonValues.
 * @param T the type returned by the visit methods.
 */
interface JsonVisitor<T> {
    /**
     * Visit JsonValue object of specific type.
     * @return value of type [T].
     */
    fun visit(value: JsonValue): T
    fun visit(string: JsonString): T
    fun visit(number: JsonNumber): T
    fun visit(boolean: JsonBoolean): T
    fun visit(nullValue: JsonNull): T
    fun visit(array: JsonArray): T
    fun visit(obj: JsonObject): T
}

/**
 * Used to validate JsonObjects.
 *
 * Implements the JsonVisitor interface with a Boolean type.
 */
// Validations
class JsonValidationVisitor : JsonVisitor<Boolean> {
    private val visitedObjects = mutableSetOf<JsonObject>()

    /**
     * Handles all types of JsonValue [value] visit for validation.
     * @return a Boolean value representing success on the validation.
     */
    override fun visit(value: JsonValue): Boolean = when (value) {
        is JsonString, is JsonNumber, is JsonBoolean, is JsonNull -> true
        is JsonArray -> visit(value) // Use the specific visit method for JsonArray
        is JsonObject -> visit(value) // Use the specific visit method for JsonObject
    }

    override fun visit(string: JsonString): Boolean = true

    override fun visit(number: JsonNumber): Boolean = true

    override fun visit(boolean: JsonBoolean): Boolean = true

    override fun visit(nullValue: JsonNull): Boolean = true

    /**
     * Visits all JsonValues contained in [array] for validation.
     * @return a Boolean value representing success on the validation.
     */
    override fun visit(array: JsonArray): Boolean {
        return array.values.all { visit(it) }
    }

    /**
     * Visits the JsonObject [obj] for validation.
     * Keys must be unique and not blank for successful validation.
     * @return a Boolean value representing success on the validation.
     */
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

    /**
     * Validates whether [jsonValue] is valid.
     * @return a Boolean value representing success on the validation.
     */
    fun isValid(jsonValue: JsonValue): Boolean {
        visitedObjects.clear()
        return visit(jsonValue)
    }
}

/**
 * Used to check types of elements in a JsonArray.
 *
 * Implements the JsonVisitor interface with a Boolean type.
 */
class JsonTypeCheckVisitor : JsonVisitor<Boolean> {
    /**
     * Handles all types of JsonValue [value] visit for type checking.
     * @return a Boolean value representing success on the type checking.
     */
    override fun visit(value: JsonValue): Boolean = when (value) {
        is JsonString, is JsonNumber, is JsonBoolean, is JsonNull -> true
        is JsonArray -> visit(value)
        is JsonObject -> visit(value)
    }

    override fun visit(string: JsonString): Boolean = true
    override fun visit(number: JsonNumber): Boolean = true
    override fun visit(boolean: JsonBoolean): Boolean = true
    override fun visit(nullValue: JsonNull): Boolean = true

    /**
     * Visits the JsonArray [array] for type checking.
     * Empty JsonArray is considered successful type checking.
     * All elements must be of the same type and not null for successful type checking.
     * @return a Boolean value representing success on the validation.
     */
    override fun visit(array: JsonArray): Boolean {
        if (array.values.isEmpty()) return true
        val firstType = array.values.firstOrNull()?.let { it::class } ?: return true //handle empty
        return array.values.all { it::class == firstType && it !is JsonNull }
    }

    /**
     * Visits all properties of [obj] for type checking.
     * @return a Boolean value representing success on the type checking.
     */
    override fun visit(obj: JsonObject): Boolean {
        return obj.properties.values.all { visit(it) }
    }

    /**
     * Checks [jsonArray] for type checking.
     * @return a Boolean value representing success on the type checking.
     */
    fun checkArray(jsonArray: JsonArray): Boolean {
        return visit(jsonArray)
    }
}
