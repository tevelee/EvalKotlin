package sample

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateInterpreterTest {
    @Test
    fun `whenEvaluated thenReturnsPlainValue`() {
        val integer = DataType(listOf(Literal { value, _ -> value.toIntOrNull() }))
        val interpreter = TypedInterpreter(listOf(integer), listOf(addOperator()))

        val print = Pattern<String, TemplateInterpreter<String>>(Keyword("{{") + Variable<Any>("body") + Keyword("}}")) {
            variables, evaluator, _ -> evaluator.print(variables["body"] ?: "")
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