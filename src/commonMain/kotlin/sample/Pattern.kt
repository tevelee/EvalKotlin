package sample

interface PatternElement {
    fun matches(prefix: String): MatchResult
}

operator fun PatternElement.plus(other: PatternElement) = listOf(this, other)
operator fun List<PatternElement>.plus(other: PatternElement) = this.toMutableList().apply { add(other) }.toList()

typealias MatcherBlock<T, E> = (variables: Map<String, Any>, evaluator: E) -> T?

class Pattern<T, I: Interpreter<*>>(val elements: List<PatternElement>, val matcher: MatcherBlock<T, I>) {
    fun matches(string: String, from: Int = 0, interpreter: I): MatchResult {
        val result = Matcher<T, I>(elements).match(string, from) { matcher(it, interpreter) }
        if (result is MatchResult.ExactMatch<*>) {
            //TODO: print debug info
        }
        return result
    }
}
