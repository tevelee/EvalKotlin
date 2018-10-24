package sample

fun <T> List<T>.transform(block: MutableList<T>.() -> Unit): List<T> = toMutableList().apply { this.block() }
fun <T> List<T>.replace(index: Int, item: T): List<T> = transform {
    removeAt(index)
    add(index, item)
}
inline fun <T> T.applyIf(condition: Boolean, transform: (T) -> T): T = if (condition) transform(this) else this
