**Guide to Using the Kotlin JSON Manipulation Library**

This guide will walk you through how to use the custom Kotlin library for generating, manipulating, and validating JSON data using in-memory models. This library does **not** rely on external JSON parsing or serialization libraries, providing a deeper understanding of JSON handling.

**Table of Contents**

1. Core JSON Value Classes
2. Instantiating JSON Values Programmatically
3. Converting Kotlin Objects to JSON Values (Reflection-based)
    * Supported Kotlin Types
4. Filtering JSON Objects and Arrays
    * Filtering JsonObject
    * Filtering JsonArray
5. Mapping JSON Arrays
6. Using the Visitor Pattern
    * JSON Validation Visitor
    * JSON Type Check Visitor
7. Serializing JSON Values to String
8. Building HTTP/GET JSON APIs
    * Overview
    * Setting Up the Server
    * Creating Controllers
    * Defining Endpoints with @Mapping
    * Handling Path Variables with @Path
    * Handling Query Parameters with @Param
    * Automatic JSON Conversion of Return Types
    * Running and Testing Your API

---

**1\. Core JSON Value Classes**

The library defines a sealed class hierarchy to represent the fundamental JSON data types in memory:

* JsonValue: The abstract base class for all JSON types.
* JsonString(value: String): Represents a JSON string.
* JsonNumber(value: Number): Represents a JSON number (can be Int, Double, etc.).
* JsonBoolean(value: Boolean): Represents a JSON boolean (true or false).
* JsonNull: Represents a JSON null value (singleton object).
* JsonArray(values: List\<JsonValue\>): Represents a JSON array, containing a list of JsonValue objects.
* JsonObject(properties: Map\<String, JsonValue\>): Represents a JSON object, containing a map of string keys to JsonValue objects.

**2\. Instantiating JSON Values Programmatically**

You can create instances of JSON values directly using their constructors. This allows you to build complex JSON structures in code.

Kotlin
```
// Create a JSON String  
val jsonName \= JsonString("Alice")

// Create a JSON Number  
val jsonAge \= JsonNumber(30)

// Create a JSON Boolean  
val jsonActive \= JsonBoolean(true)

// Create a JSON Null  
val jsonEmail \= JsonNull

// Create a JSON Array  
val jsonHobbies \= JsonArray(listOf(  
JsonString("reading"),  
JsonString("hiking"),  
JsonString("coding")  
))

// Create a nested JSON Object  
val jsonAddress \= JsonObject(mapOf(  
"street" to JsonString("123 Main St"),  
"city" to JsonString("Anytown"),  
"zip" to JsonNumber(12345)  
))

// Create a root JSON Object  
val jsonUser \= JsonObject(mapOf(  
"name" to jsonName,  
"age" to jsonAge,  
"active" to jsonActive,  
"email" to jsonEmail,  
"hobbies" to jsonHobbies,  
"address" to jsonAddress  
))

println(jsonUser.toJsonString())  
// Output: {"name":"Alice","age":30,"active":true,"email":null,"hobbies":\["reading","hiking","coding"\],"address":{"street":"123 Main St","city":"Anytown","zip":12345}}
```

**3\. Converting Kotlin Objects to JSON Values (Reflection-based)**

The toJsonValue function allows you to automatically convert standard Kotlin objects into the library's JsonValue hierarchy using reflection. This is particularly useful for taking your application's data models and preparing them for JSON output.

Kotlin
```
fun toJsonValue(obj: Any?): JsonValue { /\* ... implementation ... \*/ }
```

**Supported Kotlin Types**

The toJsonValue function supports the following Kotlin types for automatic conversion:

* Int, Double (converted to JsonNumber)
* Boolean (converted to JsonBoolean)
* String (converted to JsonString)
* List\<T\> where T is any supported type (converted to JsonArray)
* Enum (converted to JsonString using the enum's name)
* null (converted to JsonNull)
* data classes with properties whose types are supported (converted to JsonObject)
* Map\<String, T\> where T is any supported type (converted to JsonObject)

**Example:**

Kotlin

```
data class Address(val street: String, val city: String)  
data class User(val id: Int, val name: String, val address: Address?, val tags: List\<String\>, val role: Role)  
enum class Role { ADMIN, EDITOR, VIEWER }

val address \= Address("456 Oak Ave", "Someville")  
val user \= User(2, "Bob Smith", address, listOf("dev", "kotlin"), Role.EDITOR)  
val myMap \= mapOf("status" to "success", "code" to 200, "data" to listOf(true, false))

val jsonUser \= toJsonValue(user)  
val jsonMap \= toJsonValue(myMap)  
val jsonNull \= toJsonValue(null)  
val jsonEnum \= toJsonValue(Role.ADMIN)

println("User as JSON: ${jsonUser.toJsonString()}")  
println("Map as JSON: ${jsonMap.toJsonString()}")  
println("Null as JSON: ${jsonNull.toJsonString()}")  
println("Enum as JSON: ${jsonEnum.toJsonString()}")

```

**4\. Filtering JSON Objects and Arrays**

The library provides extension functions to filter JsonObject and JsonArray instances. These operations are **non-mutating**, meaning they return *new* JsonValue instances without altering the original.

**Filtering** JsonObject

Use the filterObject extension function on a JsonObject. It takes a predicate lambda (key: String, value: JsonValue) \-\> Boolean and returns a new JsonObject containing only the properties that satisfy the predicate.

Kotlin

```
val originalObject \= JsonObject(mapOf(  
"id" to JsonNumber(1),  
"name" to JsonString("Test User"),  
"isActive" to JsonBoolean(true),  
"secret" to JsonString("hidden\_value")  
))

// Filter out the "secret" property  
val filteredObject \= originalObject.filterObject { key, \_ \-\> key \!= "secret" }

println("Original: ${originalObject.toJsonString()}")  
println("Filtered: ${filteredObject.toJsonString()}")  
// Output: {"id":1,"name":"Test User","isActive":true}
```

**Filtering** JsonArray

Use the filterArray extension function on a JsonArray. It takes a predicate lambda (value: JsonValue) \-\> Boolean and returns a new JsonArray containing only the elements that satisfy the predicate.

Kotlin

```
val originalArray \= JsonArray(listOf(  
JsonNumber(10),  
JsonString("apple"),  
JsonNumber(20),  
JsonString("banana"),  
JsonBoolean(true)  
))

// Filter to keep only JsonNumber values  
val filteredArray \= originalArray.filterArray { it is JsonNumber }

println("Original: ${originalArray.toJsonString()}")  
println("Filtered: ${filteredArray.toJsonString()}")  
// Output: \[10,20\]
```

**5\. Mapping JSON Arrays**

The map function on JsonArray allows you to transform each element of an array, producing a new JsonArray. It's also non-mutating.

Kotlin
```
val numberArray \= JsonArray(listOf(JsonNumber(1), JsonNumber(2), JsonNumber(3)))

// Double each number in the array  
val doubledArray \= numberArray.map { value \-\>  
if (value is JsonNumber) {  
JsonNumber(value.value.toInt() \* 2)  
} else {  
value // Return original if not a number  
}  
}

println("Original: ${numberArray.toJsonString()}")  
println("Mapped: ${doubledArray.toJsonString()}")  
// Output: \[2,4,6\]
```

**6\. Using the Visitor Pattern**

The library implements the Visitor pattern to facilitate adding new features that involve traversing the JSON structure recursively without modifying the core JsonValue classes.

**JSON Validation Visitor**

The JsonValidationVisitor checks for structural validity of JSON objects (e.g., non-blank and unique keys) and recursively validates nested structures.

Kotlin

```
val validationVisitor \= JsonValidationVisitor()

// Valid object  
val validJson \= JsonObject(mapOf("name" to JsonString("Alice"), "age" to JsonNumber(25)))  
println("Is valid: ${validationVisitor.isValid(validJson)}") // true

// Invalid object: empty key  
val invalidJson1 \= JsonObject(mapOf("" to JsonString("value")))  
println("Is valid: ${validationVisitor.isValid(invalidJson1)}") // false

// Invalid object: duplicate key  
val invalidJson2 \= JsonObject(mapOf("key" to JsonString("value1"), "key" to JsonString("value2")))  
println("Is valid: ${validationVisitor.isValid(invalidJson2)}") // false (due to map behavior, will only have one entry, but if constructed differently, it would fail)

// Nested invalid object  
val nestedInvalidJson \= JsonObject(mapOf(  
"data" to JsonObject(mapOf("item" to JsonString("A"), "item" to JsonString("B")))  
))  
println("Is valid: ${validationVisitor.isValid(nestedInvalidJson)}") // false
```

**JSON Type Check Visitor**

The JsonTypeCheckVisitor checks if all non-null values within a JsonArray are of the same JsonValue type.

Kotlin

```
val typeCheckVisitor \= JsonTypeCheckVisitor()

// Array of all numbers  
val intArray \= JsonArray(listOf(JsonNumber(1), JsonNumber(2), JsonNumber(3)))  
println("Is homogeneous (numbers): ${typeCheckVisitor.checkArray(intArray)}") // true

// Array of all strings  
val stringArray \= JsonArray(listOf(JsonString("a"), JsonString("b")))  
println("Is homogeneous (strings): ${typeCheckVisitor.checkArray(stringArray)}") // true

// Mixed types  
val mixedArray \= JsonArray(listOf(JsonNumber(1), JsonString("b"), JsonBoolean(true)))  
println("Is homogeneous (mixed): ${typeCheckVisitor.checkArray(mixedArray)}") // false

// Array with nulls (nulls are ignored for type checking)  
val nullArray \= JsonArray(listOf(JsonNumber(1), JsonNull, JsonNumber(3)))  
println("Is homogeneous (with nulls): ${typeCheckVisitor.checkArray(nullArray)}") // true

// Empty array  
val emptyArray \= JsonArray(emptyList())  
println("Is homogeneous (empty): ${typeCheckVisitor.checkArray(emptyArray)}") // true
```

**7\. Serializing JSON Values to String**

Every JsonValue instance can be converted into its standard JSON string representation using the toJsonString() method.

Kotlin
```
val myJsonObject \= JsonObject(mapOf(  
"product" to JsonString("Laptop"),  
"price" to JsonNumber(1200.50),  
"features" to JsonArray(listOf(  
JsonString("lightweight"),  
JsonString("fast SSD")  
)),  
"available" to JsonBoolean(true),  
"warranty" to JsonNull  
))

val jsonString \= myJsonObject.toJsonString()  
println(jsonString)  
// Output: {"product":"Laptop","price":1200.5,"features":\["lightweight","fast SSD"\],"available":true,"warranty":null}

val myJsonArray \= JsonArray(listOf(  
JsonString("item1"),  
JsonNumber(123),  
JsonObject(mapOf("nested" to JsonBoolean(false)))  
))  
println(myJsonArray.toJsonString())  
// Output: \["item1",123,{"nested":false}\]
```

**8\. Building HTTP/GET JSON APIs**

This section guides you on how to use the lightweight Kotlin framework to build HTTP/GET endpoints that automatically return JSON responses using the JSON manipulation library.

**Overview**

The framework allows you to define REST controllers with annotated methods that map to specific URL paths. It handles:

* Receiving HTTP/GET requests.
* Routing requests to the correct controller method.
* Extracting path variables and query parameters.
* Automatically converting Kotlin return values from your controller methods into JSON using the toJsonValue function.

**Setting Up the Server**

To launch your API server, you'll use the GetJson class. You need to provide it with a list of your controller classes.

Kotlin

```
// In your main function or application entry point  
fun main() {  
// Pass your controller classes to the GetJson framework  
val app \= GetJson(listOf(MyController::class, AnotherController::class))

    // Start the server on a specified port (e.g., 8080\)  
    app.start(8080)  
    println("Server is running on http://localhost:8080")  
}
```

**Creating Controllers**

Controllers are regular Kotlin classes that contain your endpoint methods. You can optionally annotate the class with @Mapping to define a base path for all endpoints within that controller.

Kotlin
```
import com.sun.net.httpserver.HttpExchange // Required for HttpExchange in Router

// Add this to your project if not already present  
annotation class Mapping(val value: String)  
annotation class Path(val value: String)  
annotation class Param(val value: String)

@Mapping("api") // Base path for all endpoints in this controller will start with "/api"  
class MyController {  
// ... endpoint methods go here ...  
}
```

**Defining Endpoints with** @Mapping

Use the @Mapping annotation on individual methods within your controller to define the specific path segment for that endpoint. The full path will be a combination of the class-level @Mapping (if present) and the method-level @Mapping.

Kotlin
```
@Mapping("api")  
class MyController {  
@Mapping("hello") // This endpoint will be accessible at /api/hello  
fun sayHello(): String {  
return "Hello from API\!"  
}

    @Mapping("data") // This endpoint will be accessible at /api/data  
    fun getData(): List\<String\> {  
        return listOf("item1", "item2", "item3")  
    }  
}
```

**Handling Path Variables with** @Path

You can define path variables in your @Mapping annotation using curly braces (e.g., {id}). To extract the value of a path variable into a function argument, use the @Path annotation on the corresponding parameter.

Kotlin
```
@Mapping("api")  
class MyController {  
@Mapping("users/{userId}") // Matches paths like /api/users/123 or /api/users/abc  
fun getUserById(@Path("userId") userId: String): String {  
return "Fetching user with ID: $userId"  
}

    @Mapping("products/{category}/{productId}") // Matches /api/products/electronics/P101  
    fun getProduct(@Path("category") category: String, @Path("productId") productId: String): Map\<String, String\> {  
        return mapOf("category" to category, "productId" to productId)  
    }  
}
```

**Important:** Ensure the name in @Path("name") exactly matches the variable name in the @Mapping path (e.g., userId matches {userId}).

**Handling Query Parameters with** @Param

To extract values from query parameters (e.g., ?name=value\&age=30), use the @Param annotation on your function arguments. The framework will automatically convert the string value to the expected type (Int, Double, Boolean, String).

Kotlin
```
@Mapping("api")  
class MyController {  
@Mapping("search") // Matches /api/search?query=kotlin\&limit=10  
fun search(@Param("query") query: String, @Param("limit") limit: Int): String {  
return "Searching for '$query' with limit $limit"  
}

    @Mapping("config") // Matches /api/config?enabled=true  
    fun getConfig(@Param("enabled") enabled: Boolean): String {  
        return "Feature enabled: $enabled"  
    }  
}
```

**Important:** If a required (non-nullable) @Param is missing in the URL, the framework will throw an IllegalArgumentException.

**Automatic JSON Conversion of Return Types**

One of the core features of this framework is its ability to automatically convert the return value of your controller methods into a JSON string using the toJsonValue function. This means you can return standard Kotlin types, and the framework will handle the serialization.

**Supported Return Types:**

* Int, Double, Boolean, String

* List\<T\> (where T is a supported type)

* Map\<String, T\> (where T is a supported type)

* Enum

* data classes

* null

**Example:**

Kotlin

```
@Mapping("api")  
class MyController {  
// Returns a List\<Int\>, converted to JSON array: \[1,2,3\]  
@Mapping("ints")  
fun demo(): List\<Int\> \= listOf(1, 2, 3)

    // Returns a Pair\<String, String\>, converted to JSON object: {"first": "um", "second": "dois"}  
    @Mapping("pair")  
    fun obj(): Pair\<String, String\> \= Pair("um", "dois")

    // Returns a String, converted to JSON string: "hello\!"  
    @Mapping("hello-string")  
    fun helloString(): String \= "hello\!"

    // Returns a Map, converted to JSON object  
    @Mapping("status")  
    fun getStatus(): Map\<String, Any\> \= mapOf("status" to "OK", "timestamp" to System.currentTimeMillis())

    // Returns a data class, converted to JSON object  
    data class Product(val id: Int, val name: String, val price: Double)  
    @Mapping("product")  
    fun getProduct(): Product \= Product(101, "Laptop", 1200.50)  
}
```

**Running and Testing Your API**

1. **Compile your Kotlin code:**

2. Bash

`kotlinc YourMainFile.kt \-include-runtime \-d YourApp.jar`

3.
4. (Replace YourMainFile.kt and YourApp.jar with your actual file names)

5. **Run the compiled application:**

6. Bash

`java \-jar YourApp.jar`

7.
8. You should see "Server started on port 8080" in your console.

9. **Test with** curl **or your browser:**

    * curl http://localhost:8080/api/ints

    * curl http://localhost:8080/api/pair

    * curl http://localhost:8080/api/path/your\_path\_variable\_value

    * curl http://localhost:8080/api/args?n=3\&text=ABC

This framework provides a basic yet powerful way to expose JSON APIs from your Kotlin applications with minimal setup.

