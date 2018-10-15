package sample

class Matcher<T, E: Evaluator<*>>(val elements: List<PatternElement>) {
    data class ActiveVariable(val name: String, val options: VariableOptions, var value: String)

    fun match(string: String, from: Int = 0, renderer: (variables: Map<String, Any>) -> T?): MatchResult {
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
                    if (currentlyActiveVariable == null && element is GenericVariable<*, *>)
                        currentlyActiveVariable = ActiveVariable(element.name, element.options, String())
                    if (result.exhaustive) {
                        if (currentlyActiveVariable != null) {
                            currentlyActiveVariable.value += remainder.substring(0, 1)
                            remainder = remainder.drop(1)
                        }
                        if (remainder.isEmpty()) {
                            if (currentlyActiveVariable != null)
                                variables[currentlyActiveVariable.name] = currentlyActiveVariable.value
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
                        variables[currentlyActiveVariable.name] = currentlyActiveVariable.value.trim()
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