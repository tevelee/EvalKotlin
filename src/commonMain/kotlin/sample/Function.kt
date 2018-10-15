package sample

interface FunctionInterface<E> {
    fun convert(input: String, interpreter: E): Any?
}

class Function<T, E : Interpreter<Any>>(val patterns: List<Pattern<T, E>>) : FunctionInterface<E> {
    override fun convert(input: String, interpreter: E): Any? {
        val result = matchStatement(patterns, input, interpreter)
        return when (result) {
            is MatchResult.ExactMatch<*> -> result.output
            else -> null
        }
    }
}

data class MatchStatementResult<T, I : Interpreter<Any>>(val element: Pattern<T, I>, val result: MatchResult)

fun <T, I : Interpreter<Any>> matchStatement(statements: List<Pattern<T, I>>, input: String, interpreter: I, from: Int = 0): MatchResult {
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
