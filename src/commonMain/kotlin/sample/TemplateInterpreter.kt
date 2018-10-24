package sample

open class TemplateInterpreter<T>(open val statements: List<Pattern<T, TemplateInterpreter<T>>> = listOf(),
                                  open val interpreter: TypedInterpreter = TypedInterpreter(),
                                  override val context: Context) : Interpreter<T> {
    override val interpreterForEvaluatingVariables: Interpreter<*>
        get() { return interpreter }

    override fun evaluate(expression: String): T = evaluate(expression, Context())
    override fun evaluateOrNull(expression: String): T? = evaluateOrNull(expression, Context())

    override fun evaluate(expression: String, context: Context): T = evaluateOrNull(expression, context) as T
    override fun evaluateOrNull(expression: String, context: Context): T? =
        error("Shouldn't instantiate `TemplateInterpreter` directly. Please subclass with a dedicated type instead")

    fun evaluate(expression: String, context: Context, reducer: TemplateReducer<T>): T?{
        context.merge(this.context)
        var output = reducer.initialValue

        var position = 0
        do {
            val result = matchStatement(statements, expression, this, context, position)
            when (result) {
                is MatchResult.NoMatch, is MatchResult.PossibleMatch -> {
                    output = reducer.reduceCharacter(output, expression[position])
                    position += 1
                }
                is MatchResult.ExactMatch<*> -> {
                    output = reducer.reduceValue(output, result.output as T)
                    position += result.length
                }
                else -> error("Invalid result")
            }
        } while (position < expression.length)

        return output
    }

    override fun print(input: Any): String = interpreter.print(input)
}

class StringTemplateInterpreter(
    override val statements: List<Pattern<String, TemplateInterpreter<String>>> = listOf(),
    override val interpreter: TypedInterpreter = TypedInterpreter(),
    override val context: Context = Context()
) : TemplateInterpreter<String>(statements, interpreter, context) {
    override fun evaluateOrNull(expression: String, context: Context): String? {
        val reducer = TemplateReducer("",
            { a, b -> a + b },
            { a, b -> a + b })
        return evaluate(expression, context, reducer)
    }
}