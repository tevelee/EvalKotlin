package sample

sealed class MatchResult {
    object NoMatch : MatchResult()
    object PossibleMatch : MatchResult()
    data class ExactMatch<T>(val length: Int, val output: T, val variables: Map<String, Any>): MatchResult()
    data class AnyMatch(val exhaustive: Boolean): MatchResult()
}

