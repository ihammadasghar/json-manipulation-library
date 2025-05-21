# Guide to Using the Kotlin JSON Manipulation Library

This guide will walk you through how to use the custom Kotlin library for generating, manipulating, and validating JSON data using in-memory models. This library does **not** rely on external JSON parsing or serialization libraries, providing a deeper understanding of JSON handling.

## Table of Contents

1.  [Core JSON Value Classes](#1-core-json-value-classes)

2.  [Instantiating JSON Values Programmatically](#2-instantiating-json-values-programmatically)

3.  [Converting Kotlin Objects to JSON Values (Reflection-based)](#3-converting-kotlin-objects-to-json-values-reflection-based)

    * [Supported Kotlin Types](#supported-kotlin-types)

4.  [Filtering JSON Objects and Arrays](#4-filtering-json-objects-and-arrays)

    * [Filtering `JsonObject`](#filtering-jsonobject)

    * [Filtering `JsonArray`](#filtering-jsonarray)

5.  [Mapping JSON Arrays](#5-mapping-json-arrays)

6.  [Using the Visitor Pattern](#6-using-the-visitor-pattern)

    * [JSON Validation Visitor](#json-validation-visitor)

    * [JSON Type Check Visitor](#json-type-check-visitor)

7.  [Serializing JSON Values to String](#7-serializing-json-values-to-string)

## 1. Core JSON Value Classes

The library defines a sealed class hierarchy to represent the fundamental JSON data types in memory:

* `JsonValue`: The abstract base class for all JSON types.

* `JsonString(value: String)`: Represents a JSON string.

* `JsonNumber(value: Number)`: Represents a JSON number (can be `Int`, `Double`, etc.).

* `JsonBoolean(value: Boolean)`: Represents a JSON boolean (`true` or `false`).

* `JsonNull`: Represents a JSON null value (singleton object).

* `JsonArray(values: List<JsonValue>)`: Represents a JSON array, containing a list of `JsonValue` objects.

* `JsonObject(properties: Map<String, JsonValue>)`: Represents a JSON object, containing a map of string keys to `JsonValue` objects.

## 2. Instantiating JSON Values Programmatically

You can create instances of JSON values directly using their constructors. This allows you to build complex JSON structures in code.

```kotlin
// Create a JSON String
val jsonName = JsonString("Alice")

// Create a JSON Number
val jsonAge = JsonNumber(30)

// Create a JSON Boolean
val jsonActive = JsonBoolean(true)

// Create a JSON Null
val jsonEmail = JsonNull

// Create a JSON Array
val jsonHobbies = JsonArray(listOf(
    JsonString("reading"),
    JsonString("hiking"),
    JsonString("coding")
))

// Create a nested JSON Object
val jsonAddress = JsonObject(mapOf(
    "street" to JsonString("123 Main St"),
    "city" to JsonString("Anytown"),
    "zip" to JsonNumber(12345)
))

// Create a root JSON Object
val jsonUser = JsonObject(mapOf(
    "name" to jsonName,
    "age" to jsonAge,
    "active" to jsonActive,
    "email" to jsonEmail,
    "hobbies" to jsonHobbies,
    "address" to jsonAddress
))

println(jsonUser.toJsonString())
// Output: {"name":"Alice","age":30,"active":true,"email":null,"hobbies":["reading","hiking","coding"],"address":{"street":"123 Main St","city":"Anytown","zip":12345}}