package sample

fun <T> List<T>.transform(block: MutableList<T>.() -> Unit): List<T> = toMutableList().apply { this.block() }
fun <T> List<T>.replace(index: Int, item: T): List<T> = transform {
    removeAt(index)
    add(index, item)
}
inline fun <T> T.applyIf(condition: Boolean, transform: (T) -> T): T = if (condition) transform(this) else this

operator fun PatternElement.plus(other: PatternElement) = listOf(this, other)
operator fun List<PatternElement>.plus(other: PatternElement) = transform { add(other) }

operator fun String.plus(other: PatternElement) = Keyword(this) + other
operator fun PatternElement.plus(other: String) = this + Keyword(other)
operator fun List<PatternElement>.plus(other: String) = this + Keyword(other)
