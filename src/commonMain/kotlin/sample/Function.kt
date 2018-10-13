package sample

interface FunctionInterface<E> {
    fun convert(input: String, interpreter: E): Any?
}

class Function<T, E : Interpreter<*>>(val patterns: List<Pattern<T, E>>) : FunctionInterface<E> {
    override fun convert(input: String, interpreter: E): Any? {
        val result = matchStatement(patterns, input, interpreter)
        return when (result) {
            is MatchResult.ExactMatch<*> -> result.output
            else -> null
        }
    }
}

data class MatchStatementResult<T, E : Interpreter<*>>(val element: Pattern<T, E>, val result: MatchResult)

fun <T, E : Interpreter<*>> matchStatement(statements: List<Pattern<T, E>>, input: String, interpreter: E, from: Int = 0): MatchResult {
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
