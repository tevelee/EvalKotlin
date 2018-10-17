package sample

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateInterpreterTest {
    @Test
    fun whenEvaluated_thenReturnsPlainValue() {
        val integer = DataType(listOf(Literal { value, _ -> value.toIntOrNull() }))
        val interpreter = TypedInterpreter(listOf(integer), listOf(addOperator()))

        val print = Pattern<String, TemplateInterpreter<String>>(Keyword("{{") + Variable<String>("body", VariableOptions(interpreted = false)) + Keyword("}}")) {
            variables, interpreter, context -> val body = variables["body"] as? String ?: return@Pattern null
            interpreter.evaluate(body, context)
        }
        val templateInterpreter = StringTemplateInterpreter(listOf(print), interpreter)

        val value = templateInterpreter.evaluate("a {{ 1 + 2 }} b")

        assertEquals("a 3 b", value)
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
}