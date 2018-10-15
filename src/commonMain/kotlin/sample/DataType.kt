package sample

class DataType<T>(private val literals: List<Literal<T>>) {
    fun convert(input: String, interpreter: TypedInterpreter): Any? = literals.map { it.convert(input, interpreter) }.firstOrNull()
}