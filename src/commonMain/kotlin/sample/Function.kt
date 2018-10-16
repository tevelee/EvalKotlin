package sample

interface FunctionInterface<E> {
    fun convert(input: String, interpreter: E): Any?
}

class Function<T>(private val patterns: List<Pattern<T, TypedInterpreter>>) : FunctionInterface<TypedInterpreter> {
    override fun convert(input: String, interpreter: TypedInterpreter): Any? {
        val result = matchStatement(patterns, input, interpreter)
        return when (result) {
            is MatchResult.ExactMatch<*> -> result.output
            else -> null
        }
    }
}

data class MatchStatementResult<T, I : Interpreter<*>>(val element: Pattern<T, I>, val result: MatchResult)

fun <T, I : Interpreter<*>> matchStatement(statements: List<Pattern<T, I>>, input: String, interpreter: I, from: Int = 0): MatchResult {
    val results = statements.asSequence().map {
        MatchStatementResult(it, it.matches(input, from, interpreter))
    }
    val matchingElement = results.firstOrNull { it.result is MatchResult.ExactMatch<*> }
    return when {
        matchingElement != null -> matchingElement.result
        results.any { it.result is MatchResult.PossibleMatch } -> MatchResult.PossibleMatch()
        else -> MatchResult.NoMatch()
    }
}
