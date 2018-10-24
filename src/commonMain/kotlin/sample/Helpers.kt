package sample

fun <T> List<T>.transform(block: MutableList<T>.() -> Unit): List<T> = toMutableList().apply { this.block() }
fun <T> List<T>.replace(index: Int, item: T): List<T> = transform {
    removeAt(index)
    add(index, item)
}
inline fun <T> T.applyIf(condition: Boolean, transform: (T) -> T): T = if (condition) transform(this) else this

fun VariableOptions.transform(block: VariableOptions.Builder.() -> Unit) = builder().apply(block).build()
fun <T> Variable<T>.transform(block: Variable.Builder<T>.() -> Unit) = builder().apply(block).build()
