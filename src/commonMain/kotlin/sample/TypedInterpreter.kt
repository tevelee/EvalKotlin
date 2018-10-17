package sample

interface Evaluator<EvaluatedType> {
    fun evaluate(expression: String): EvaluatedType
    fun evaluateOrNull(expression: String): EvaluatedType?
}

interface Interpreter<EvaluatedType>: Evaluator<EvaluatedType> {
    val context: Context
    val interpreterForEvaluatingVariables: Interpreter<*>
    fun evaluate(expression: String, context: Context = Context()): EvaluatedType
    fun evaluateOrNull(expression: String, context: Context = Context()): EvaluatedType?
}

class TypedInterpreter(private val dataTypes: List<DataType<*>> = listOf(),
                       private val functions: List<Function<*>> = listOf(),
                       override var context: Context = Context()
) : Interpreter<Any> {
    private val dataTypeCache: MutableMap<String, DataType<*>> = mutableMapOf()
    private val functionsCache: MutableMap<String, Function<*>> = mutableMapOf()

    override val interpreterForEvaluatingVariables: Interpreter<*>
        get() { return this }

    override fun evaluate(expression: String): Any = evaluate(expression, Context())
    override fun evaluateOrNull(expression: String): Any? = evaluateOrNull(expression, Context())

    override fun evaluate(expression: String, context: Context): Any = evaluateOrNull(expression, context) as Any
    override fun evaluateOrNull(expression: String, context: Context): Any? {
        val fullContext = this.context.merge(context)
        val input = expression.trim()
        return functionFromCache(input, fullContext)
            ?: dataTypeFromCache(input)
            ?: dataType(input)
            ?: variable(input, fullContext)
            ?: function(input, fullContext)
    }

    private fun dataType(expression: String, interpreter: TypedInterpreter = this) =
        find(dataTypes, { it.convert(expression, interpreter) }, { dataTypeCache[expression] = it })

    private fun function(expression: String, context: Context, interpreter: TypedInterpreter = this) =
        find(functions.reversed(), { it.convert(expression, interpreter, context) }, { functionsCache[expression] = it })

    private fun variable(expression: String, context: Context): Any? =
        find(context.variables, expression) { it }

    private fun dataTypeFromCache(expression: String, interpreter: TypedInterpreter = this) =
        find(dataTypeCache, expression) { it.convert(expression, interpreter) }

    private fun functionFromCache(expression: String, context: Context, interpreter: TypedInterpreter = this) =
        find(functionsCache, expression) { it.convert(expression, interpreter, context) }

    private fun <T, R> find(source: List<T>, match: (T) -> R, cache: (T) -> Unit) = source
        .asSequence()
        .map { it to match(it) }
        .filter { it.second != null }
        .firstOrNull()
        .alsoIfNotNull { cache(it.first) }
        ?.second

    private fun <T, R> find(source: Map<String, T>, key: String, call: (T) -> R) = source
        .entries
        .firstOrNull { it.key == key }
        ?.run { call(value) }
}

inline fun <T> T?.alsoIfNotNull(block: (T) -> Unit): T? {
    if (this != null) block(this)
    return this
}
