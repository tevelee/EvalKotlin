package sample

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateInterpreterTest {
    @Test
    fun whenEvaluatedWithPrintStatement_thenReturnsReplacedValue() {
        val integer = DataType(listOf(Literal { value, _ -> value.toIntOrNull() }))
        val interpreter = TypedInterpreter(listOf(integer), listOf(addOperator()))

        val templateInterpreter = StringTemplateInterpreter(listOf(printPattern()), interpreter)

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

    private fun addOperator(): Pattern<Int, TypedInterpreter> = infixOperator("+") { a: Int, b: Int -> a + b }
    private fun equalsOperator(): Pattern<Boolean, TypedInterpreter> = infixOperator("==") { a: Int, b: Int -> a == b }

    private fun <L, R, T> infixOperator(symbol: String, reduce: (L, R) -> T): Pattern<T, TypedInterpreter> = Pattern(Variable<L>("lhs") + symbol + Variable<R>("rhs"), PatternOptions(backwardMatch = true)) {
        val lhs = variables["lhs"] as? L ?: return@Pattern null
        val rhs = variables["rhs"] as? R ?: return@Pattern null
        reduce(lhs, rhs)
    }

    private fun printPattern() = Pattern<String, TemplateInterpreter<String>>(Keyword("{{") + Variable<Any>("body") + Keyword("}}")) {
        evaluator.print(variables["body"] ?: "")
    }

    private fun ifStatement() = Pattern<String, TemplateInterpreter<String>>(Keyword("{%") + Keyword("if") + Variable<Boolean>("condition") + Keyword("%}") + TemplateVariable("body") + Keyword("{% endif %}")) {
        val condition = variables["condition"] as? Boolean ?: return@Pattern null
        val body = variables["body"] as? String ?: return@Pattern null
        if (condition) evaluator.evaluate(body, context) else null
    }
}