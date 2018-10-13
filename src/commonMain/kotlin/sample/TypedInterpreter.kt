package sample

interface Evaluator<EvaluatedType> {
    fun evaluate(expression: String): EvaluatedType
}

interface Interpreter<EvaluatedType>: Evaluator<EvaluatedType>

class TypedInterpreter(private val dataTypes: List<DataTypeInterface> = listOf(),
                       private val functions: List<Function<*, TypedInterpreter>> = listOf()) : Interpreter<Any?> {
    override fun evaluate(expression: String): Any? {
        return function(expression) ?: dataType(expression)
    }

    private fun dataType(expression: String) = dataTypes
        .asSequence()
        .map { it.convert(expression, this) }
        .filterNotNull()
        .firstOrNull()

    private fun function(expression: String) = functions
        .asSequence()
        .map { it.convert(expression, this) }
        .filterNotNull()
        .firstOrNull()
}