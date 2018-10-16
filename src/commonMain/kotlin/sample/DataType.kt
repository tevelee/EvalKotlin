package sample

class DataType<T: Any>(private val literals: List<Literal<T>>) {
    fun convert(input: String, interpreter: TypedInterpreter): Any? =
        literals
            .asSequence()
            .mapNotNull { it.convert(input, interpreter) }
            .firstOrNull()
}