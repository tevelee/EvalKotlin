package sample

sealed class MatchResult {
    class NoMatch: MatchResult()
    class PossibleMatch: MatchResult()
    class ExactMatch<T>(val length: Int, val output: T, val variables: Map<String, Any>): MatchResult()
    class AnyMatch(val exhaustive: Boolean): MatchResult()
}

