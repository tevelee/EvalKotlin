package sample

interface VariableInterface {
    val name: String
}

class Variable(override val name: String, val exhaustive: Boolean = false): PatternElement, VariableInterface {
    override fun matches(prefix: String): MatchResult {
        return MatchResult.AnyMatch(exhaustive)
    }
}
