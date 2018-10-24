package sample

class DataType<T: Any>(private val literals: List<Literal<T>>, val print: (value: Any) -> String? = { (it as? T)?.toString() }) {
    fun convert(input: String, interpreter: TypedInterpreter): Any? =
        literals
            .asSequence()
            .mapNotNull { it.convert(input, interpreter) }
            .firstOrNull()
}