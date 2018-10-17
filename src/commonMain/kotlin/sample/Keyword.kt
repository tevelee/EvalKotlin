package sample

class Keyword(val name: String,
              val type: Type = Type.GENERIC): PatternElement {
    enum class Type {
        GENERIC,
        OPENING_TAG,
        CLOSING_TAG
    }

    override fun matches(prefix: String, options: PatternOptions): MatchResult {
        val checker: (String, String) -> Boolean = if (options.backwardMatch) { a, b -> a.endsWith(b) } else { a, b -> a.startsWith(b) }
        return when {
            name == prefix || checker.invoke(prefix, name) -> MatchResult.ExactMatch(name.length, name, mapOf())
            checker.invoke(name, prefix) -> MatchResult.PossibleMatch()
            else -> MatchResult.NoMatch()
        }
    }
}