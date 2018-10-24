package sample

class ExpressionInfo(val input: String,
                     val output: Any,
                     val patterns: String,
                     val variables: Map<String, Any>) {
    override fun toString(): String = "(" +
            "input=$input, " +
            "output=$output, " +
            "patterns=$patterns, " +
            "variables:$variables" +
            ")"
}

class ExpressionStack(val variables: Map<String, Any>,
                      val debugInfo: Map<String, ExpressionInfo>)

class Context(var variables: MutableMap<String, Any> = mutableMapOf(),
              var debugInfo: MutableMap<String, ExpressionInfo> = mutableMapOf(),
              val stack: MutableList<ExpressionStack> = mutableListOf()) {
    fun merge(context: Context) {
        variables.putAll(context.variables)
        debugInfo.putAll(context.debugInfo)
    }

    fun push() = stack.add(ExpressionStack(variables, debugInfo))
    fun pop() = stack.removeAt(stack.lastIndex).let {
        debugInfo = it.debugInfo.toMutableMap()
        variables = it.variables.toMutableMap()
    }
}
