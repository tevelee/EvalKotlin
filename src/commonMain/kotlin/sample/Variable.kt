package sample

data class VariableOptions(val interpreted : Boolean = true,
                           val trimmed : Boolean = true,
                           val exhaustive : Boolean = false,
                           val acceptsNullValue : Boolean = false) {
    class Builder(var interpreted: Boolean,
                  var trimmed : Boolean,
                  var exhaustive : Boolean,
                  var acceptsNullValue : Boolean) {
        fun build() = VariableOptions(interpreted, trimmed, exhaustive, acceptsNullValue)
    }
    fun builder(): Builder = Builder(interpreted, trimmed, exhaustive, acceptsNullValue)
}

fun VariableOptions.transform(block: VariableOptions.Builder.() -> Unit) = builder().apply(block).build()

open class Variable<T>(
    val name: String,
    val options: VariableOptions = VariableOptions(),
    val map: (input: Any, interpreter: Interpreter<*>) -> T? = { input, _ -> input as? T }
): PatternElement {
    override fun matches(prefix: String, options: PatternOptions): MatchResult {
        return MatchResult.AnyMatch(this.options.exhaustive)
    }

    class Builder<T>(var name: String,
                     var options: VariableOptions = VariableOptions(),
                     var map: (input: Any, interpreter: Interpreter<*>) -> T?) {
        fun build() = Variable(name, options, map)
    }
    fun builder(): Builder<T> = Builder(name, options, map)
}

fun <T> Variable<T>.transform(block: Variable.Builder<T>.() -> Unit) = builder().apply(block).build()

class TemplateVariable(
    name: String,
    options: VariableOptions = VariableOptions(),
    map: (input: String, interpreter: StringTemplateInterpreter) -> String? = { value, _ -> value }
) : Variable<String>(name, options.transform { interpreted = false }, { value, interpreter ->
    val stringValue = value as? String ?: ""
    val stringInterpreter = interpreter as StringTemplateInterpreter
    val result = if (options.interpreted) stringInterpreter.evaluate(stringValue) else stringValue
    map(result, stringInterpreter)
})
