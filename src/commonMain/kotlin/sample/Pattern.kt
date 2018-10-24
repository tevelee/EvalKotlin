package sample

interface PatternElement {
    fun matches(prefix: String, options: PatternOptions): MatchResult
}

operator fun PatternElement.plus(other: PatternElement) = listOf(this, other)
operator fun List<PatternElement>.plus(other: PatternElement) = transform { add(other) }

typealias MatcherBlock<T, E> = (variables: Map<String, Any>, evaluator: E, context: Context) -> T?

data class PatternOptions(val backwardMatch: Boolean = false)

class Pattern<T, I: Interpreter<*>>(elements: List<PatternElement>,
                                    val options: PatternOptions = PatternOptions(),
                                    val matcher: MatcherBlock<T, I>) {
    val elements = replaceLastElementNotToBeShortestMatch(elements)

    private fun replaceLastElementNotToBeShortestMatch(elements: List<PatternElement>): List<PatternElement> {
        val index = if (options.backwardMatch) 0 else elements.lastIndex
        val variable = elements[index] as? Variable<*> ?: return elements
        val newVariable= variable.transform { options = options.transform { exhaustive = true } }
        return elements.replace(index, newVariable)
    }

    fun matches(string: String, startIndex: Int = 0, interpreter: I, context: Context, connectedRanges: List<IntRange>): MatchResult {
        val variableProcessor = VariableProcessor(interpreter.interpreterForEvaluatingVariables, context)
        val result = Matcher<T>(elements, options, variableProcessor).match(string, startIndex, connectedRanges) { matcher(it, interpreter, context) }
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

fun <T> List<T>.transform(block: MutableList<T>.() -> Unit): List<T> = toMutableList().apply { this.block() }
fun <T> List<T>.replace(index: Int, item: T): List<T> = transform {
    removeAt(index)
    add(index, item)
}