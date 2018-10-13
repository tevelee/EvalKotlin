package sample

class Literal<T>(val convertBlock: (input: String, interpreter: TypedInterpreter) -> T?) {
    constructor (check: String, convertsTo: () -> T) : this(convertBlock = { input, _ -> if (input == check) convertsTo() else null })

    fun convert(input: String, interpreter: TypedInterpreter): T? {
        return convertBlock(input, interpreter)
    }
}
