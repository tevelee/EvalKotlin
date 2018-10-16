package sample

data class ActiveVariable(val name: String, var value: String, val metadata: Variable<*>)

class Matcher<T>(
    val elements: List<PatternElement>,
    val options: PatternOptions,
    val processor: VariableProcessor
) {
    fun match(string: String, from: Int = 0, renderer: (variables: Map<String, Any>) -> T?): MatchResult {
        var currentlyActiveVariable: ActiveVariable? = null
        var elementIndex = initialIndex()
        var remainder = string.substring(startIndex = from)
        val variables: MutableMap<String, Any> = mutableMapOf()
        do {
            val element = elements[elementIndex]
            val result = element.matches(remainder, options)
            when (result) {
                is MatchResult.NoMatch ->
                    remainder = proceed(currentlyActiveVariable, remainder) ?: return MatchResult.NoMatch()
                is MatchResult.PossibleMatch ->
                    return MatchResult.PossibleMatch()
                is MatchResult.AnyMatch -> {
                    if (currentlyActiveVariable == null && element is Variable<*>)
                        currentlyActiveVariable = ActiveVariable(element.name, String(), element)
                    if (result.exhaustive) {
                        remainder = proceed(currentlyActiveVariable, remainder) as String
                        if (remainder.isEmpty()) {
                            registerVariable(currentlyActiveVariable, variables) ?: return MatchResult.PossibleMatch()
                            elementIndex = nextElement(elementIndex)
                        }
                    }
                    else
                        elementIndex = nextElement(elementIndex)
                }
                is MatchResult.ExactMatch<*> -> {
                    variables.putAll(result.variables)
                    registerVariable(currentlyActiveVariable, variables) ?: return MatchResult.NoMatch()
                    currentlyActiveVariable = null
                    elementIndex = nextElement(elementIndex)
                    remainder = drop(remainder, result.length)
                    remainder = trim(remainder)
                }
            }
        } while (notFinished(elementIndex))

        val output = renderer(variables)
        return if (output != null)
            MatchResult.ExactMatch(string.length - from - remainder.length, output, variables)
        else
            MatchResult.NoMatch()
    }

    private fun initialIndex(): Int = if (options.backwardMatch) elements.lastIndex else 0
    private fun nextElement(elementIndex: Int): Int = if (options.backwardMatch) elementIndex - 1 else elementIndex + 1
    private fun drop(remainder: String, length: Int) = if (options.backwardMatch) remainder.dropLast(length) else remainder.drop(length)
    private fun notFinished(elementIndex: Int): Boolean = if (options.backwardMatch) elementIndex >= 0 else elementIndex <= elements.lastIndex
    private fun trim(remainder: String): String = if (options.backwardMatch) remainder.trimStart() else remainder.trimEnd()
    private fun appendNextCharacterToVariable(currentlyActiveVariable: ActiveVariable, remainder: String) {
        if (options.backwardMatch) {
            currentlyActiveVariable.value = remainder.last() + currentlyActiveVariable.value
        } else {
            currentlyActiveVariable.value += remainder.first()
        }
    }
    private fun proceed(currentlyActiveVariable: ActiveVariable?, remainder: String): String? {
        if (currentlyActiveVariable != null) {
            appendNextCharacterToVariable(currentlyActiveVariable, remainder)
            return drop(remainder, 1)
        }
        return null
    }
    private fun registerVariable(currentlyActiveVariable: ActiveVariable?, variables: MutableMap<String, Any>): Boolean? {
        if (currentlyActiveVariable != null) {
            val value = processor.process(currentlyActiveVariable)
            value?.let { variables[currentlyActiveVariable.name] = it }
            return !currentlyActiveVariable.metadata.options.acceptsNullValue && value != null
        }
        return null
    }
}