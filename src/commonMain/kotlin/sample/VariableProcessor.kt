package sample

class VariableProcessor(private val interpreter: Interpreter<*>, private val context: Context) {
    fun process(variable: ActiveVariable): Any? {
        val value = variable.value.applyIf(variable.metadata.options.trimmed) { variable.value.trim() }
        if (variable.metadata.options.interpreted) {
            val variableInterpreter = interpreter.interpreterForEvaluatingVariables
            return variableInterpreter
                .evaluateOrNull(value, context)
                ?.let { variable.metadata.map(it, variableInterpreter) }
        }
        return variable.metadata.map(value, interpreter)
    }
}
