package sample

class Context(val variables: Map<String, Any> = mapOf()) {
    fun merge(context: Context) =
        Context(variables.transform { putAll(context.variables) })
}

fun <K, V> Map<K, V>.transform(block: MutableMap<K, V>.() -> Unit): Map<K, V> = toMutableMap().apply { this.block() }