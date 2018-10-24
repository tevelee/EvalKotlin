package sample

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateInterpreterTest {
    @Test
    fun whenEvaluatedWithPrintStatement_thenReturnsReplacedValue() {
        val integer = DataType(listOf(Literal { value, _ -> value.toIntOrNull() }))
        val interpreter = TypedInterpreter(listOf(integer), listOf(addOperator()))

        val templateInterpreter = StringTemplateInterpreter(listOf(printFunction()), interpreter)

        val value = templateInterpreter.evaluate("a {{ 1 + 2 }} b")

        assertEquals("a 3 b", value)
    }

    @Test
    fun whenEvaluatedWithIfStatement_thenReturnsReplacedValue() {
        val integer = DataType(listOf(Literal { value, _ -> value.toIntOrNull() }))
        val boolean = DataType(listOf(Literal("true") { true }, Literal("false") { false }))
        val interpreter = TypedInterpreter(listOf(integer, boolean), listOf(equalsOperator()))

        val templateInterpreter = StringTemplateInterpreter(listOf(ifStatement()), interpreter)

        val value = templateInterpreter.evaluate("a {% if 1 == 1 %}x{% endif %} b")

        assertEquals("a x b", value)
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

    private fun equalsOperator(): Function<Boolean> {
        val pattern = Pattern<Boolean, TypedInterpreter>(
            Variable<Boolean>("lhs") + Keyword("==") + Variable<Int>("rhs"), PatternOptions(backwardMatch = true)) { variables, _, _ ->
            val lhs = variables["lhs"] as? Int ?: return@Pattern null
            val rhs = variables["rhs"] as? Int ?: return@Pattern null
            lhs == rhs
        }
        return Function(listOf(pattern))
    }

    private fun printFunction() = Pattern<String, TemplateInterpreter<String>>(Keyword("{{") + Variable<Any>("body") + Keyword("}}")) {
            variables, evaluator, _ -> evaluator.print(variables["body"] ?: "")
    }

    private fun ifStatement() = Pattern<String, TemplateInterpreter<String>>(Keyword("{%") + Keyword("if") + Variable<Boolean>("condition") + Keyword("%}") + TemplateVariable("body") + Keyword("{% endif %}")) { variables, _, _ ->
        val condition = variables["condition"] as? Boolean ?: return@Pattern null
        val body = variables["body"] as? String ?: return@Pattern null
        if (condition) body else null
    }
}