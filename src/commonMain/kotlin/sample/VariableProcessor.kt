package sample

class  VariableProcessor(private val interpreter: Interpreter<Any>, private val context: Context) {
    fun process(variable: ActiveVariable): Any? {
        val value = variable.value.applyIf(variable.metadata.options.trimmed) { variable.value.trim() }
        if (variable.metadata.options.interpreted) {
            val variableInterpreter = interpreter.interpreterForEvaluatingVariables
            val output = variableInterpreter.evaluate(value, context)
            return variable.metadata.map(output, variableInterpreter)
        }
        return variable.metadata.map(value, interpreter)
    }
}

inline fun <T> T.applyIf(condition: Boolean, transform: (T) -> T): T = if (condition) transform(this) else this