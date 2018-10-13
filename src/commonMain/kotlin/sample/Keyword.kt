package sample

class Keyword(val name: String): PatternElement {
    override fun matches(prefix: String): MatchResult {
        return when {
            name == prefix || prefix.startsWith(name) -> MatchResult.ExactMatch(name.length, name, mapOf())
            name.startsWith(prefix) -> MatchResult.PossibleMatch()
            else -> MatchResult.NoMatch()
        }
    }
}