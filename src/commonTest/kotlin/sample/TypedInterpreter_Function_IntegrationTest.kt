package sample

import kotlin.test.Test
import kotlin.test.assertEquals
import sample.Keyword.Type.*

class TypedInterpreter_Function_IntegrationTest {
    @Test
    fun whenEvaluatedWithFunction_thenReturnsTheComputedValue() {
        val string = DataType(listOf(
            Literal("a") { "a" },
            Literal("b") { "b" }
        ))
        val interpreter = TypedInterpreter(listOf(string), listOf(concatOperator()))

        val value = interpreter.evaluate("a + b")

        assertEquals( "ab", value)
    }

    @Test
    fun whenEvaluatedWithFunctionWithMultipleUse_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(multiplyOperator()))

        val value = interpreter.evaluate("1 * 2 * 3")

        assertEquals( 6, value)
    }

    @Test
    fun whenEvaluatedWithFunctionBackwards_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(multiplyOperator(), addOperator()))

        val value = interpreter.evaluate("2 + 3 * 4")

        assertEquals( 14, value)
    }

    @Test
    fun whenEvaluatedWithNonCommutativeFunctionBackwards_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(subtractOperator()))

        val value = interpreter.evaluate("6 - 4 - 2")

        assertEquals( 0, value)
    }

    @Test
    fun whenEvaluatedWithNonCommutativeFunctionWithParentheses_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(parentheses(), subtractOperator(), addOperator()))

        val value = interpreter.evaluate("6 - (4 + 2)")

        assertEquals( 0, value)
    }

    @Test
    fun whenEvaluatedWithFunctionWithSimpleParentheses_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(parentheses(), addOperator()))

        val value = interpreter.evaluate("(2 + 3)")

        assertEquals( 5, value)
    }

    @Test
    fun whenEvaluatedWithFunctionWithParentheses_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(parentheses(), multiplyOperator(), addOperator()))

        val value = interpreter.evaluate("(2 + 3) * 4")

        assertEquals( 20, value)
    }

    @Test
    fun whenEvaluatedWithMultipleOperators_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(parentheses(), subtractOperator()))

        val value = interpreter.evaluate("6 - (4 - 2)")

        assertEquals( 4, value)
    }

    @Test
    fun whenEvaluatedWithFunctionWithEmbeddedParentheses_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(parentheses(), subtractOperator()))

        val value = interpreter.evaluate("12 - (6 - (4 - 2))")

        assertEquals( 8, value)
    }

    @Test
    fun whenEvaluatedWithContextVariables_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(multiplyOperator()), Context(mutableMapOf("y" to 2)))

        val value = interpreter.evaluate("x * y * 3", Context(mutableMapOf("x" to 6)))

        assertEquals( 36, value)
    }

    private fun concatOperator(): Function<String> {
        val pattern = Pattern<String, TypedInterpreter>(Variable<String>("lhs") + Keyword("+") + Variable<String>("rhs")) { variables, _, _ ->
            val lhs = variables["lhs"] as? String ?: return@Pattern null
            val rhs = variables["rhs"] as? String ?: return@Pattern null
            lhs + rhs
        }
        return Function(listOf(pattern))
    }

    private fun multiplyOperator(): Function<Int> {
        val pattern = Pattern<Int, TypedInterpreter>(
            Variable<Int>("lhs") + Keyword("*") + Variable<Int>("rhs"), PatternOptions(backwardMatch = true)) { variables, _, _ ->
            val lhs = variables["lhs"] as? Int ?: return@Pattern null
            val rhs = variables["rhs"] as? Int ?: return@Pattern null
            lhs * rhs
        }
        return Function(listOf(pattern))
    }

    private fun addOperator(): Function<Int> {
        val pattern = Pattern<Int, TypedInterpreter>(
            Variable<Int>("lhs") + Keyword("+") + Variable<Int>("rhs"), PatternOptions(backwardMatch = true)) { variables, _, _ ->
            val lhs = variables["lhs"] as? Int ?: return@Pattern null
            val rhs = variables["rhs"] as? Int ?: return@Pattern null
            lhs + rhs
        }
        return Function(listOf(pattern))
    }

    private fun subtractOperator(): Function<Int> {
        val pattern = Pattern<Int, TypedInterpreter>(
            Variable<Int>("lhs") + Keyword("-") + Variable<Int>("rhs"), PatternOptions(backwardMatch = true)) { variables, _, _ ->
            val lhs = variables["lhs"] as? Int ?: return@Pattern null
            val rhs = variables["rhs"] as? Int ?: return@Pattern null
            lhs - rhs
        }
        return Function(listOf(pattern))
    }

    private fun parentheses(): Function<Any> {
        val pattern = Pattern<Any, TypedInterpreter>(
            Keyword("(", OPENING_TAG) + Variable<Int>("body") + Keyword(")", CLOSING_TAG)) { variables, _, _ ->
            variables["body"]
        }
        return Function(listOf(pattern))
    }
}