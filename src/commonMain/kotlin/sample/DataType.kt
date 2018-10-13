package sample

interface DataTypeInterface {
    fun convert(input: String, interpreter: TypedInterpreter): Any?
}

class DataType<T>(private val literals: List<Literal<T>>) : DataTypeInterface {
    override fun convert(input: String, interpreter: TypedInterpreter): Any? = literals.map { it.convert(input, interpreter) }.firstOrNull()
}