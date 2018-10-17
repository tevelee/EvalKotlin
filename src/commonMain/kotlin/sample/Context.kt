package sample

class Context(val variables: Map<String, Any> = mapOf()) {
    fun merge(context: Context) =
        Context(variables.toMutableMap().apply { putAll(context.variables) }.toMap())
}
