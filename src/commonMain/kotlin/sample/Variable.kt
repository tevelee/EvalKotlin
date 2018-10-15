package sample

data class VariableOptions(val interpreted : Boolean = true,
                           val trimmed : Boolean = true,
                           val exhaustive : Boolean = false,
                           val acceptsNullValue : Boolean = false)

open class GenericVariable<T, E: Evaluator<*>>(
    val name: String,
    val options: VariableOptions = VariableOptions(),
    val map: (input: Any, interpreter: E) -> T? = { input, _ -> input as? T }
): PatternElement {
    override fun matches(prefix: String): MatchResult {
        return MatchResult.AnyMatch(options.exhaustive)
    }
}

class Variable<T>(name: String,
                  options: VariableOptions = VariableOptions(),
                  map: (input: Any, interpreter: TypedInterpreter) -> T? = { input, _ -> input as? T }) :
    GenericVariable<T, TypedInterpreter>(name, options, map)

