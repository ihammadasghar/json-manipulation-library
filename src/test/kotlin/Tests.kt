import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * This class contains all Tests.
 */
@RunWith(Enclosed::class)
class Tests {
    class JsonStringTest {
        @Test
        fun testToJsonString() {
            assertEquals("\"test\"", JsonString("test").toJsonString())
            assertEquals("\"te\\nst\"", JsonString("te\nst").toJsonString())
        }

        @Test
        fun testEquals() {
            val testStr1 = JsonString("Test")
            val testStr2 = JsonString("tseT")

            assertTrue(testStr1.equals(testStr1))
            assertFalse(testStr1.equals("Test"))
            assertTrue(testStr1.equals(JsonString("Test")))
            assertFalse(testStr1.equals(testStr2))
        }
    }

    class JsonNumberTest {
        @Test
        fun testToJsonString() {
            assertEquals("64", JsonNumber(64).toJsonString())
        }

        @Test
        fun testEquals() {
            val testNum1 = JsonNumber(16)
            val testNum2 = JsonNumber(32)

            assertTrue(testNum1.equals(testNum1))
            assertFalse(testNum1.equals(16))
            assertTrue(testNum1.equals(JsonNumber(16)))
            assertFalse(testNum1.equals(testNum2))
        }
    }

    class JsonBooleanTest {
        @Test
        fun testToJsonString() {
            assertEquals("false", JsonBoolean(false).toJsonString())
        }

        @Test
        fun testEquals() {
            val testBool1 = JsonBoolean(true)
            val testBool2 = JsonBoolean(false)

            assertTrue(testBool1.equals(testBool1))
            assertFalse(testBool1.equals(true))
            assertTrue(testBool1.equals(JsonBoolean(true)))
            assertFalse(testBool1.equals(testBool2))
        }
    }

    class JsonNullTest {
        @Test
        fun testToJsonString() {
            assertEquals("null", JsonNull.toJsonString())
        }

        @Test
        fun testEquals() {
            val testNull = JsonNull

            assertTrue(testNull.equals(testNull))
            assertFalse(testNull.equals(null))
            assertTrue(testNull.equals(JsonNull))
        }
    }

    class JsonArrayTest {
        @Test
        fun testToJsonString() {
            val testArr = JsonArray(listOf(JsonString("test"), JsonNumber(64), JsonBoolean(true)))
            val testStr = "[" + JsonString("test").toJsonString() + "," +
                    JsonNumber(64).toJsonString() + "," +
                    JsonBoolean(true).toJsonString() + "]"
            assertEquals(testStr, testArr.toJsonString())
        }

        @Test
        fun testEquals() {
            val testArr1 = JsonArray(listOf(JsonString("test"), JsonNumber(64), JsonBoolean(true)))
            val testArr2 = JsonArray(listOf(JsonString("etts"), JsonNumber(32), JsonBoolean(false)))

            assertTrue(testArr1.equals(testArr1))
            assertFalse(testArr1.equals(listOf(JsonString("test"), JsonNumber(64), JsonBoolean(true))))
            assertTrue(testArr1.equals(JsonArray(listOf(JsonString("test"), JsonNumber(64), JsonBoolean(true)))))
            assertFalse(testArr1.equals(testArr2))
        }

        @Test
        fun testMap() {
            val testArr = JsonArray(listOf(JsonString("test"), JsonNumber(64), JsonBoolean(true)))

            assertEquals("[16,16,16]", testArr.map { JsonNumber(16) }.toJsonString())
        }

        @Test
        fun testArrayFilter() {
            val testArr = JsonArray(listOf(JsonString("test"), JsonNumber(64), JsonBoolean(true)))

            assertEquals("[64]", testArr.filterArray { it is JsonNumber && it.value == 64 }.toJsonString())
        }
    }

    class JsonObjectTest {
        @Test
        fun testToJsonString() {
            val testObj = JsonObject(
                mapOf(
                    "testStr" to JsonString("test"),
                    "testNum" to JsonNumber(32),
                    "testBool" to JsonBoolean(true)
                )
            )

            assertEquals("{\"testStr\":\"test\",\"testNum\":32,\"testBool\":true}", testObj.toJsonString())
        }

        @Test
        fun testEquals() {
            val testObj1 = JsonObject(
                mapOf(
                    "testStr" to JsonString("test"),
                    "testNum" to JsonNumber(32),
                    "testBool" to JsonBoolean(true)
                )
            )
            val testObj2 = JsonObject(
                mapOf(
                    "testStr" to JsonString("etts"),
                    "testNum" to JsonNumber(48),
                    "testBool" to JsonBoolean(true)
                )
            )

            assertTrue(testObj1.equals(testObj1))
            assertFalse(
                testObj1.equals(
                    mapOf(
                        "testStr" to JsonString("test"),
                        "testNum" to JsonNumber(32),
                        "testBool" to JsonBoolean(true)
                    )
                )
            )
            assertTrue(
                testObj1.equals(
                    JsonObject(
                        mapOf(
                            "testStr" to JsonString("test"),
                            "testNum" to JsonNumber(32),
                            "testBool" to JsonBoolean(true)
                        )
                    )
                )
            )
            assertFalse(testObj1.equals(testObj2))
        }

        @Test
        fun testObjectFilter() {
            val testObj = JsonObject(
                mapOf(
                    "testStr" to JsonString("test"),
                    "testNum" to JsonNumber(32),
                    "testBool" to JsonBoolean(true)
                )
            )

            assertEquals("{\"testStr\":\"test\"}", testObj.filterObject { key, _ -> key == "testStr" }.toJsonString())
        }
    }

    class UtilsTest {
        @Test
        fun testEscapeString() {
            assertEquals("te\\nst", escapeString("te\nst"))
        }

        enum class TestEnum {
            TEST1, TEST2
        }

        data class TestDataClass(val testStr: String, val testNum: Int)

        @Test
        fun testToJsonValue() {
            assertEquals(JsonNull, toJsonValue(null))
            assertEquals(JsonNumber(12.4), toJsonValue(12.4))
            assertEquals(JsonBoolean(true), toJsonValue(true))
            assertEquals(JsonString("test"), toJsonValue("test"))
            assertEquals(JsonArray(listOf(JsonString("test"), JsonNumber(16))), toJsonValue(listOf("test", 16)))
            assertEquals(JsonString("TEST2"), toJsonValue(TestEnum.TEST2))

            assertEquals(
                JsonObject(
                    mapOf(
                        "testStr" to JsonString("test"),
                        "testNum" to JsonNumber(32)
                    )
                ), toJsonValue(
                    mapOf(
                        "testStr" to "test",
                        "testNum" to 32
                    )
                )
            )

            assertEquals(
                JsonObject(
                    mapOf(
                        "testStr" to JsonString("testStr"),
                        "testNum" to JsonNumber(64),
                    )
                ), toJsonValue(TestDataClass("testStr", 64))
            )
        }
    }

    class JsonVisitorsTest {
        @Test
        fun testJsonObjectValidationVisitor() {
            val testObj1 = JsonObject(mapOf("testStr" to JsonString("test"), "testNum" to JsonNumber(16)))
            val testObj2 = JsonObject(mapOf("" to JsonString("test")))

            assertTrue(JsonValidationVisitor().visit(testObj1))
            assertFalse(JsonValidationVisitor().visit(testObj2))
        }

        @Test
        fun testJsonArrayTypeCheckVisitor() {
            val testArray1 = JsonArray(listOf(JsonString("testStr1"), JsonString("testStr2")))
            val testArray2 = JsonArray(listOf())
            val testArray3 = JsonArray(listOf(JsonString("testStr"), JsonNumber(16)))
            val testArray4 = JsonArray(listOf(JsonNull, JsonString("testStr")))

            assertTrue(JsonTypeCheckVisitor().checkArray(testArray1))
            assertTrue(JsonTypeCheckVisitor().checkArray(testArray2))
            assertFalse(JsonTypeCheckVisitor().checkArray(testArray3))
            assertFalse(JsonTypeCheckVisitor().checkArray(testArray4))
        }
    }

    class GetJSONTest {
        @Mapping("testController")
        class TestController {
            @Mapping("testMapping")
            fun testMapping(): List<String> = listOf("testStr", "testStr2")

            @Mapping("testPath/{testPathvar}")
            fun testPath(@Path("testPathvar") testPathvar: String): String = testPathvar

            @Mapping("testParam")
            fun testParam(@Param("testNum") testNum: Int, @Param("testStr") testStr: String): Map<String, String> =
                mapOf(testStr to testStr.repeat(testNum))
        }

        @Test
        fun testInvalidURL() {
            val server = GetJson(listOf(TestController::class))
            val client = OkHttpClient()

            server.start(8081)

            val testRequest = Request.Builder().url("http://localhost:8081/testController/testInvalidURL").build()
            client.newCall(testRequest).execute().use { response ->
                assertEquals(404, response.code)
                assertEquals("Not Found", response.body?.string())
            }
        }

        @Test
        fun testMapping() {
            val client = OkHttpClient()
            val server = GetJson(listOf(TestController::class))

            server.start(8082)

            val testRequest = Request.Builder().url("http://localhost:8082/testController/testMapping").build()
            client.newCall(testRequest).execute().use { response ->
                assertEquals(200, response.code)
                assertEquals("[\"testStr\",\"testStr2\"]", response.body?.string())
            }
        }

        @Test
        fun testUnsupportedMethod() {
            val client = OkHttpClient()
            val server = GetJson(listOf(TestController::class))

            server.start(8083)

            val testRequest = Request.Builder().url("http://localhost:8083/testController/testMapping")
                .post(okhttp3.RequestBody.create(null, ByteArray(0))).build()
            client.newCall(testRequest).execute().use { response ->
                assertEquals(405, response.code)
                assertEquals("Method Not Allowed", response.body?.string())
            }
        }

        @Test
        fun testPath() {
            val client = OkHttpClient()
            val server = GetJson(listOf(TestController::class))

            server.start(8084)

            val testRequest = Request.Builder().url("http://localhost:8084/testController/testPath/testStr").build()
            client.newCall(testRequest).execute().use { response ->
                assertEquals(200, response.code)
                assertEquals("\"testStr\"", response.body?.string())
            }
        }

        @Test
        fun testParam() {
            val client = OkHttpClient()
            val server = GetJson(listOf(TestController::class))

            server.start(8085)

            val testRequest = Request.Builder().url("http://localhost:8085/testController/testParam?testNum=2&testStr=test").build()
            client.newCall(testRequest).execute().use { response ->
                assertEquals(200, response.code)
                assertEquals("{\"test\":\"testtest\"}", response.body?.string())
            }
        }

        @Test
        fun testMissingParam() {
            val client = OkHttpClient()
            val server = GetJson(listOf(TestController::class))

            server.start(8086)

            val testRequest = Request.Builder().url("http://localhost:8086/testController/testParam?testNum=2").build()
            client.newCall(testRequest).execute().use { response ->
                assertEquals(500, response.code)
                assertNotNull(response.body?.string()?.contains("Missing required parameter: testStr"))
            }
        }

        @Test
        fun testInvalidParam() {
            val client = OkHttpClient()
            val server = GetJson(listOf(TestController::class))

            server.start(8087)

            val testRequest = Request.Builder().url("http://localhost:8087/testController/testParam?testNum=invalid&testStr=test").build()
            client.newCall(testRequest).execute().use { response ->
                assertEquals(500, response.code)
                assertNotNull(response.body?.string()?.contains("NumberFormatException"))
            }
        }
    }
}