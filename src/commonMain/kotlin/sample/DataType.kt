package sample

class DataType<T: Any>(private val literals: List<Literal<T>>, private val printer: (value: T) -> String? = { it.toString() }) {
    fun convert(input: String, interpreter: TypedInterpreter): Any? =
        literals
            .asSequence()
            .mapNotNull { it.convert(input, interpreter) }
            .firstOrNull()

    fun print(value: Any): String? {
        val tValue = value as? T ?: return value.toString()
        return printer(tValue)
    }
}