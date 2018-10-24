package sample

interface PatternElement {
    fun matches(prefix: String, options: PatternOptions): MatchResult
}

operator fun PatternElement.plus(other: PatternElement) = listOf(this, other)
operator fun List<PatternElement>.plus(other: PatternElement) = transform { add(other) }

data class PatternOptions(val backwardMatch: Boolean = false)
data class PatternBody<E>(val variables: Map<String, Any>, val evaluator: E, val context: Context)

class Pattern<T, I: Interpreter<*>>(elements: List<PatternElement>,
                                    private val options: PatternOptions = PatternOptions(),
                                    val matcher: PatternBody<I>.() -> T?) {
    val elements = replaceLastElementNotToBeShortestMatch(elements)

    private fun replaceLastElementNotToBeShortestMatch(elements: List<PatternElement>): List<PatternElement> {
        val index = if (options.backwardMatch) 0 else elements.lastIndex
        val variable = elements[index] as? Variable<*> ?: return elements
        val newVariable= variable.copy(options = variable.options.copy(exhaustive = true))
        return elements.replace(index, newVariable)
    }

    fun matches(string: String,
                startIndex: Int = 0,
                interpreter: I,
                context: Context,
                connectedRanges: List<IntRange>): MatchResult {
        val variableProcessor = VariableProcessor(interpreter, context)
        val matcher = Matcher<T>(elements, options, variableProcessor)
        val result = matcher.match(string, startIndex, connectedRanges) {
            PatternBody(it, interpreter, context).matcher()
        }
        if (result is MatchResult.ExactMatch<*>) {
            context.debugInfo[string] = ExpressionInfo(string, result.output!!, pattern(), result.variables)
        }
        return result
    }

    private fun pattern(): String {
        return elements.map {
            return when (it) {
                is Keyword -> it.name
                is Variable<*> -> "{${it.name}}"
                else -> ""
            }
        }.joinToString(" ")
    }
}
