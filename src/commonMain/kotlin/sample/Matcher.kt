package sample

data class ActiveVariable(val name: String, var value: String, val metadata: Variable<*>)

class Matcher<T>(
    val elements: List<PatternElement>,
    val options: PatternOptions,
    val processor: VariableProcessor
) {
    fun match(string: String, startIndex: Int = 0, connectedRanges: List<IntRange>, renderer: (variables: Map<String, Any>) -> T?): MatchResult {
        var currentlyActiveVariable: ActiveVariable? = null
        var elementIndex = initialIndex()
        val trimmed = string.substring(startIndex = startIndex)
        var remainder = trimmed
        val variables: MutableMap<String, Any> = mutableMapOf()
        do {
            val element = elements[elementIndex]
            val result = element.matches(remainder, options)
            when (result) {
                is MatchResult.NoMatch -> {
                    currentlyActiveVariable ?: return MatchResult.NoMatch
                    remainder = proceed(currentlyActiveVariable, remainder)
                }
                is MatchResult.PossibleMatch ->
                    return MatchResult.PossibleMatch
                is MatchResult.AnyMatch -> {
                    if (currentlyActiveVariable == null && element is Variable<*>)
                        currentlyActiveVariable = ActiveVariable(element.name, String(), element)
                    if (result.exhaustive) {
                        if (currentlyActiveVariable != null)
                            remainder = proceed(currentlyActiveVariable, remainder)
                        if (remainder.isEmpty()) {
                            registerVariable(currentlyActiveVariable, variables) ?: return MatchResult.PossibleMatch
                            elementIndex = nextElement(elementIndex)
                        }
                    } else
                        elementIndex = nextElement(elementIndex)
                }
                is MatchResult.ExactMatch<*> -> {
                    val position = if (options.backwardMatch) remainder.length else trimmed.length - remainder.length
                    val isOpeningOrClosingKeyword = element is Keyword && element.type != Keyword.Type.GENERIC
                    if (connectedRanges.any { it.contains(position) } && !isOpeningOrClosingKeyword) {
                        if (currentlyActiveVariable != null)
                            remainder = proceed(currentlyActiveVariable, remainder)
                    } else {
                        variables.putAll(result.variables)
                        registerVariable(currentlyActiveVariable, variables)
                        currentlyActiveVariable = null
                        elementIndex = nextElement(elementIndex)
                        remainder = drop(remainder, result.length)
                        remainder = trim(remainder)
                    }
                }
            }
        } while (notFinished(elementIndex))

        val output = renderer(variables)
        return if (output != null)
            MatchResult.ExactMatch(string.length - startIndex - remainder.length, output, variables)
        else
            MatchResult.NoMatch
    }

    private fun initialIndex(): Int = if (options.backwardMatch) elements.lastIndex else 0
    private fun nextElement(elementIndex: Int): Int = if (options.backwardMatch) elementIndex - 1 else elementIndex + 1
    private fun drop(remainder: String, length: Int) =
        if (options.backwardMatch) remainder.dropLast(length) else remainder.drop(length)

    private fun notFinished(elementIndex: Int): Boolean =
        if (options.backwardMatch) elementIndex >= 0 else elementIndex <= elements.lastIndex

    private fun trim(remainder: String): String =
        if (options.backwardMatch) remainder.trimStart() else remainder.trimEnd()

    private fun appendNextCharacterToVariable(currentlyActiveVariable: ActiveVariable, remainder: String) {
        if (options.backwardMatch) {
            currentlyActiveVariable.value = remainder.last() + currentlyActiveVariable.value
        } else {
            currentlyActiveVariable.value += remainder.first()
        }
    }

    private fun proceed(currentlyActiveVariable: ActiveVariable, remainder: String): String {
        appendNextCharacterToVariable(currentlyActiveVariable, remainder)
        return drop(remainder, 1)
    }

    private fun registerVariable(
        currentlyActiveVariable: ActiveVariable?,
        variables: MutableMap<String, Any>
    ): Boolean? {
        if (currentlyActiveVariable != null) {
            val value = processor.process(currentlyActiveVariable)
            value?.let { variables[currentlyActiveVariable.name] = it }
            return if (value == null) currentlyActiveVariable.metadata.options.acceptsNullValue else false
        }
        return null
    }
}