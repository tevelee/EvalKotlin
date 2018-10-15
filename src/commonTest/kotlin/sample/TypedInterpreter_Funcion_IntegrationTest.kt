package sample

import kotlin.test.Test
import kotlin.test.assertEquals

class TypedInterpreter_Function_IntegrationTest {
    @Test
    fun whenEvaluatedWithFunction_thenReturnsThePlainValue() {
        val pattern = Pattern<String, TypedInterpreter>(Variable<String>("lhs") + Keyword("+") + Variable<String>("rhs", VariableOptions(exhaustive = true))) { variables, _ ->
            val lhs = variables["lhs"] as? String
            val rhs = variables["rhs"] as? String
            if (lhs != null && rhs != null) lhs + rhs else null
        }
        val plus = Function(listOf(pattern))
        val string = DataType(listOf(
            Literal("a") { "a" },
            Literal("b") { "b" }
        ))
        val interpreter = TypedInterpreter(listOf(string), listOf(plus))

        val value = interpreter.evaluate("a + b")

        assertEquals( "ab", value)
    }
}