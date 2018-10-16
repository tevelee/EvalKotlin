package sample

data class VariableOptions(val interpreted : Boolean = true,
                           val trimmed : Boolean = true,
                           val exhaustive : Boolean = false,
                           val acceptsNullValue : Boolean = false)

open class Variable<T>(
    val name: String,
    val options: VariableOptions = VariableOptions(),
    val map: (input: Any, interpreter: Interpreter<*>) -> T? = { input, _ -> input as? T }
): PatternElement {
    override fun matches(prefix: String, options: PatternOptions): MatchResult {
        return MatchResult.AnyMatch(this.options.exhaustive)
    }
}

