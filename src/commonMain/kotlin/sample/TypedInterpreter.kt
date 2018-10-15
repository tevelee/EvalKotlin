package sample

interface Evaluator<EvaluatedType> {
    fun evaluate(expression: String): EvaluatedType
    fun evaluateOrNull(expression: String): EvaluatedType?
}

interface Interpreter<EvaluatedType>: Evaluator<EvaluatedType> {
    val context: Context
    val interpreterForEvaluatingVariables: Interpreter<Any>
    fun evaluate(expression: String, context: Context = Context()): EvaluatedType
    fun evaluateOrNull(expression: String, context: Context = Context()): EvaluatedType?
}

class TypedInterpreter(private val dataTypes: List<DataType<*>> = listOf(),
                       private val functions: List<Function<*, Interpreter<Any>>> = listOf(),
                       override val context: Context = Context()
) : Interpreter<Any> {
    override val interpreterForEvaluatingVariables: Interpreter<Any>
        get() { return this }

    override fun evaluate(expression: String): Any = evaluate(expression, Context())
    override fun evaluateOrNull(expression: String): Any? = evaluateOrNull(expression, Context())

    override fun evaluate(expression: String, context: Context): Any = evaluateOrNull(expression, context) as Any
    override fun evaluateOrNull(expression: String, context: Context): Any? {
        this.context.merge(context)

        val input = expression.trim()
        return function(input) ?: dataType(input)
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