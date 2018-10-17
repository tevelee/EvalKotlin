package sample

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
    fun whenEvaluatedWithContextVariables_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(multiplyOperator()), Context(mapOf("y" to 2)))

        val value = interpreter.evaluate("x * y * 3", Context(mapOf("x" to 6)))

        assertEquals( 36, value)
    }

    private fun concatOperator(): Function<String> {
        val pattern = Pattern<String, TypedInterpreter>(Variable<String>("lhs") + Keyword("+") + Variable<String>("rhs", VariableOptions(exhaustive = true))) { variables, _ ->
            val lhs = variables["lhs"] as? String ?: return@Pattern null
            val rhs = variables["rhs"] as? String ?: return@Pattern null
            lhs + rhs
        }
        return Function(listOf(pattern))
    }

    private fun multiplyOperator(): Function<Int> {
        val pattern = Pattern<Int, TypedInterpreter>(
            Variable<Int>("lhs", VariableOptions(exhaustive = true)) + Keyword("*") + Variable<Int>("rhs"), PatternOptions(backwardMatch = true)) { variables, _ ->
            val lhs = variables["lhs"] as? Int ?: return@Pattern null
            val rhs = variables["rhs"] as? Int ?: return@Pattern null
            lhs * rhs
        }
        return Function(listOf(pattern))
    }

    private fun addOperator(): Function<Int> {
        val pattern = Pattern<Int, TypedInterpreter>(
            Variable<Int>("lhs", VariableOptions(exhaustive = true)) + Keyword("+") + Variable<Int>("rhs"), PatternOptions(backwardMatch = true)) { variables, _ ->
            val lhs = variables["lhs"] as? Int ?: return@Pattern null
            val rhs = variables["rhs"] as? Int ?: return@Pattern null
            lhs + rhs
        }
        return Function(listOf(pattern))
    }
}