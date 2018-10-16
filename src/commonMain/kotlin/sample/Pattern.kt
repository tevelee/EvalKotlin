package sample

interface PatternElement {
    fun matches(prefix: String, options: PatternOptions): MatchResult
}

operator fun PatternElement.plus(other: PatternElement) = listOf(this, other)
operator fun List<PatternElement>.plus(other: PatternElement) = this.toMutableList().apply { add(other) }.toList()

typealias MatcherBlock<T, E> = (variables: Map<String, Any>, evaluator: E) -> T?

data class PatternOptions(val backwardMatch: Boolean = false)

class Pattern<T, I: Interpreter<*>>(val elements: List<PatternElement>,
                                    val options: PatternOptions = PatternOptions(),
                                    val context: Context = Context(),
                                    val matcher: MatcherBlock<T, I>) {
    fun matches(string: String, from: Int = 0, interpreter: I): MatchResult {
        val variableProcessor = VariableProcessor(interpreter.interpreterForEvaluatingVariables, context)
        val result = Matcher<T>(elements, options, variableProcessor).match(string, from) { matcher(it, interpreter) }
        if (result is MatchResult.ExactMatch<*>) {
            //TODO: print debug info
        }
        return result
    }
}
