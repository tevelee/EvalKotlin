package sample

import kotlin.test.Test
import kotlin.test.assertEquals

class TypedInterpreter_Function_IntegrationTest {
    @Test
    fun whenEvaluatedWithFunction_thenReturnsThePlainValue() {
        val string = DataType(listOf(
            Literal("a") { "a" },
            Literal("b") { "b" }
        ))
        val interpreter = TypedInterpreter(listOf(string), listOf(concatOperator()))

        val value = interpreter.evaluate("a + b")

        assertEquals( "ab", value)
    }

//    @Test
//    fun whenEvaluatedWithFunctionBackwards_thenReturnsThePlainValue() {
//        val integer = DataType<Int>(listOf())
//        val interpreter = TypedInterpreter(listOf(integer), listOf(multiplyOperator()))
//
//        val value = interpreter.evaluate("1 * 2 * 3")
//
//        assertEquals( 6, value)
//    }

    interface plusable {
        fun plus(other: plusable): plusable
    }

    fun concatOperator(): Function<String, TypedInterpreter> {
        val pattern = Pattern<String, TypedInterpreter>(Variable<String>("lhs") + Keyword("+") + Variable<String>("rhs", VariableOptions(exhaustive = true))) { variables, _ ->
            val lhs = variables["lhs"].takeIf { it is String } as String
            val rhs = variables["rhs"].takeIf { it is String } as String
            lhs + rhs
        }
        return Function(listOf(pattern))
    }

    fun multiplyOperator(): Function<Int, TypedInterpreter> {
        val pattern = Pattern<Int, TypedInterpreter>(
            Variable<Int>("lhs") + Keyword("+") + Variable<Int>("rhs", VariableOptions(exhaustive = true))) { variables, _ ->
            val lhs = variables["lhs"].takeIf { it is Int } as Int
            val rhs = variables["rhs"].takeIf { it is Int } as Int
            lhs * rhs
        }
        return Function(listOf(pattern))
    }
}