package sample

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TypedInterpreter_DataType_IntegrationTest {
    @Test
    fun whenEvaluatedWithPlainString_thenReturnsTheSameTypedValue() {
        val stringLiteral = Literal { input, _ -> input }
        val stringType = DataType(listOf(stringLiteral))
        val interpreter = TypedInterpreter(listOf(stringType))

        val value = interpreter.evaluate("test")

        assertEquals(value, "test")
    }

    @Test
    fun whenEvaluatedWithPlainInteger_thenReturnsTheSameTypedValue() {
        val integerLiteral = Literal { input, _ -> input.toIntOrNull() }
        val integerType = DataType(listOf(integerLiteral))
        val interpreter = TypedInterpreter(listOf(integerType))

        val value = interpreter.evaluate("2")

        assertEquals(value, 2)
    }

    @Test
    fun whenEvaluatedWithBooleanTrue_thenReturnsTheSameTypedValue() {
        val trueLiteral = Literal("true") { true }
        val booleanType = DataType(listOf(trueLiteral))
        val interpreter = TypedInterpreter(listOf(booleanType))

        val value = interpreter.evaluate("true")

        assertEquals(value, true)
    }

    @Test
    fun whenEvaluatedWithBooleanFalse_thenReturnsTheSameTypedValue() {
        val falseLiteral = Literal("false") { false }
        val booleanType = DataType(listOf(falseLiteral ))
        val interpreter = TypedInterpreter(listOf(booleanType))

        val value = interpreter.evaluate("false")

        assertEquals(value, false)
    }

    @Test
    fun whenEvaluatedWithNoTypes_thenReturnsNull() {
        val interpreter = TypedInterpreter(listOf())

        val value = interpreter.evaluate("anything")

        assertNull(value)
    }

    @Test
    fun whenEvaluatedWithMultipleTypes_thenAllParseCorrectly() {
        val stringLiteral = Literal { input, _ -> input }
        val stringType = DataType(listOf(stringLiteral))

        val integerLiteral = Literal { input, _ -> input.toIntOrNull() }
        val integerType = DataType(listOf(integerLiteral))

        val trueLiteral = Literal("true") { true }
        val booleanType = DataType(listOf(trueLiteral))

        val interpreter = TypedInterpreter(listOf(booleanType, integerType, stringType))

        val value1 = interpreter.evaluate("true")
        val value2 = interpreter.evaluate("5")
        val value3 = interpreter.evaluate("teve")

        assertEquals(value1, true)
        assertEquals(value2, 5)
        assertEquals(value3, "teve")
    }
}