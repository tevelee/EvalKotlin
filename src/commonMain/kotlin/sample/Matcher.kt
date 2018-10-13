package sample

class Matcher(val elements: List<PatternElement>) {
    data class ActiveVariable(val metadata: VariableInterface, var value: String)

    fun <T> match(string: String, from: Int = 0, renderer: (variables: Map<String, Any>) -> T?): MatchResult {
        var currentlyActiveVariable: ActiveVariable? = null
        var elementIndex = initialIndex()
        var remainder = string.substring(startIndex = from)
        val variables: MutableMap<String, Any> = mutableMapOf()
        do {
            val element = elements[elementIndex]
            val result = element.matches(remainder)
            when (result) {
                is MatchResult.NoMatch ->
                    if (currentlyActiveVariable != null) {
                        currentlyActiveVariable.value += remainder.substring(0, 1)
                        remainder = remainder.drop(1)
                    }
                    else
                        return MatchResult.NoMatch()
                is MatchResult.PossibleMatch ->
                    return MatchResult.PossibleMatch()
                is MatchResult.AnyMatch -> {
                    if (currentlyActiveVariable == null && element is Variable)
                        currentlyActiveVariable = ActiveVariable(element, String())
                    if (result.exhaustive) {
                        if (currentlyActiveVariable != null) {
                            currentlyActiveVariable.value += remainder.substring(0, 1)
                            remainder = remainder.drop(1)
                        }
                        if (remainder.isEmpty()) {
                            if (currentlyActiveVariable != null)
                                variables[currentlyActiveVariable.metadata.name] = currentlyActiveVariable.value
                            else
                                return MatchResult.PossibleMatch()
                            elementIndex++
                        }
                    }
                    else
                        elementIndex++
                }
                is MatchResult.ExactMatch<*> -> {
                    variables.putAll(result.variables)
                    if (currentlyActiveVariable != null)
                        variables[currentlyActiveVariable.metadata.name] = currentlyActiveVariable.value.trim()
                    else
                        return MatchResult.NoMatch()
                    currentlyActiveVariable = null
                    elementIndex++
                    remainder = remainder.drop(result.length).trim()
                }
            }
        } while (notFinished(elementIndex))

        val output = renderer(variables)
        return if (output != null)
            MatchResult.ExactMatch(string.length - from - remainder.length, output, variables)
        else
            MatchResult.NoMatch()
    }

    private fun initialIndex(): Int = 0

    private fun notFinished(elementIndex: Int): Boolean = elementIndex <= elements.lastIndex
}