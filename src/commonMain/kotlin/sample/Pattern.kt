package sample

interface PatternElement {
    fun matches(prefix: String): MatchResult
}

typealias MatcherBlock<T, E> = (variables: Map<String, Any>, evaluator: E) -> T?

class Pattern<T, I: Interpreter<*>>(val elements: List<PatternElement>, val matcher: MatcherBlock<T, I>) {
    fun matches(string: String, from: Int = 0, interpreter: I): MatchResult {
        val result = Matcher(elements).match(string, from) { matcher(it, interpreter) }
        if (result is MatchResult.ExactMatch<*>) {
            //TODO: print debug info
        }
        return result
    }
}
