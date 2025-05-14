import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

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
            val testObj3 = JsonObject(mapOf("testStr" to JsonString("test"), "testStr" to JsonString("erm")))
            val testObj4 = JsonObject(
                mapOf(
                    "testStr" to JsonString("test"),
                    "testObj" to JsonObject(mapOf("testStr" to JsonString("test")))
                )
            )

            assertTrue(JsonValidationVisitor().visit(testObj1))
            assertFalse(JsonValidationVisitor().visit(testObj2))
            assertFalse(JsonValidationVisitor().visit(testObj3)) //We found that the duplicate key overwrites the other
            assertFalse(JsonValidationVisitor().visit(testObj4))
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
}