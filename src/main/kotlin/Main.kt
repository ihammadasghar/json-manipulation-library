data class Address(val street: String, val city: String)
data class User(val id: Int, val name: String, val address: Address?, val tags: List<String>, val role: Role)

enum class Role {
    ADMIN, EDITOR, VIEWER
}

fun main() {
    // Example Usage Phase 2
    val addressObject = Address("123 Main St", "Anytown")
    val userObject = User(1, "John Doe", addressObject, listOf("tag1", "tag2"), Role.EDITOR)
    val myMap = mapOf("key1" to 1, "key2" to "abc", "key3" to listOf(true, false))

    val jsonUser = toJsonValue(userObject)
    val jsonAddress = toJsonValue(addressObject)
    val jsonMap = toJsonValue(myMap)

    println("User as JSON: ${jsonUser.toJsonString()}")
    println("Address as JSON: ${jsonAddress.toJsonString()}")
    println("Map as JSON: ${jsonMap.toJsonString()}")

    val jsonList = toJsonValue(listOf(1, 2, 3, "a", "b", true))
    println("List as JSON: ${jsonList.toJsonString()}")

    val jsonNull = toJsonValue(null)
    println("Null as JSON: ${jsonNull.toJsonString()}")

    val jsonEnum = toJsonValue(Role.VIEWER)
    println("Enum as JSON: ${jsonEnum.toJsonString()}")

    // Example of a nested data class
    data class Company(val name: String, val location: Address)
    val company = Company("Acme Corp", addressObject)
    val jsonCompany = toJsonValue(company)
    println("Company as JSON: ${jsonCompany.toJsonString()}")

    // Example Usage Phase 1
    val address = JsonObject(
        mapOf(
            "street" to JsonString("123 Main St"),
            "city" to JsonString("Anytown")
        )
    )
    val user = JsonObject(
        mapOf(
            "id" to JsonNumber(1),
            "name" to JsonString("John Doe"),
            "isLoggedIn" to JsonBoolean(true),
            "preferences" to JsonArray(
                listOf(
                    JsonString("email"),
                    JsonString("notifications")
                )
            ),
            "address" to address,
            "age" to JsonNull
        )
    )

    println("\nInstantiation Example:")
    println(user.toJsonString())

    val filteredUser = user.filterObject { key, _ -> key != "address" }
    println("\nFiltering Example (Object):")
    println(filteredUser.toJsonString())

    val preferencesArray = user.properties["preferences"] as JsonArray
    val filteredPreferences = preferencesArray.filterArray { it is JsonString && (it).value == "email" }
    println("\nFiltering Example (Array):")
    println(filteredPreferences.toJsonString())

    val numberArray = JsonArray(listOf(JsonNumber(1), JsonNumber(2), JsonNumber(3)))
    val doubledArray = numberArray.map {
        if (it is JsonNumber) JsonNumber(it.value.toInt() * 2) else it
    }
    println("\nMapping Example:")
    println(doubledArray.toJsonString())

    val validationVisitor = JsonValidationVisitor()
    val validUser = JsonObject(mapOf("name" to JsonString("John"), "id" to JsonNumber(1)))
    val invalidUser1 = JsonObject(mapOf("" to JsonString("John"), "id" to JsonNumber(1))) //empty key
    val invalidUser2 = JsonObject(mapOf("name" to JsonString("John"), "name" to JsonNumber(1))) //duplicate key
    val invalidUser3 = JsonObject(mapOf("name" to JsonString("John"), "obj" to JsonObject(mapOf("name" to JsonString("John"), "name" to JsonNumber(1))))) //nested duplicate key

    println("\nVisitor - Validation Example:")
    println("Valid User: ${validationVisitor.isValid(validUser)}")
    println("Invalid User 1: ${validationVisitor.isValid(invalidUser1)}")
    println("Invalid User 2: ${validationVisitor.isValid(invalidUser2)}")
    println("Invalid User 3: ${validationVisitor.isValid(invalidUser3)}")

    // Visitor - Type Checking
    val typeCheckVisitor = JsonTypeCheckVisitor()
    val intArray = JsonArray(listOf(JsonNumber(1), JsonNumber(2), JsonNumber(3)))
    val stringArray = JsonArray(listOf(JsonString("a"), JsonString("b"), JsonString("c")))
    val mixedArray = JsonArray(listOf(JsonNumber(1), JsonString("b"), JsonBoolean(true)))
    val nullArray = JsonArray(listOf(JsonNumber(1), JsonNull, JsonNumber(3)))

    println("\nVisitor - Type Checking Example:")
    println("Int Array: ${typeCheckVisitor.checkArray(intArray)}")
    println("String Array: ${typeCheckVisitor.checkArray(stringArray)}")
    println("Mixed Array: ${typeCheckVisitor.checkArray(mixedArray)}")
    println("Null Array: ${typeCheckVisitor.checkArray(nullArray)}")

    println("\nSerialization Example:")
    println(user.toJsonString())
}
