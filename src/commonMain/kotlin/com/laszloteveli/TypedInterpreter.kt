/*
 *  Copyright (c) 2018 Laszlo Teveli.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.laszloteveli

interface Evaluator<EvaluatedType> {
    fun evaluate(expression: String): EvaluatedType
    fun evaluateOrNull(expression: String): EvaluatedType?
}

interface Printer {
    fun print(input: Any): String
}

interface Interpreter<EvaluatedType>: Evaluator<EvaluatedType>, Printer {
    val context: com.laszloteveli.Context
    val interpreterForEvaluatingVariables: Interpreter<*>
    fun evaluate(expression: String, context: com.laszloteveli.Context = com.laszloteveli.Context()): EvaluatedType
    fun evaluateOrNull(expression: String, context: com.laszloteveli.Context = com.laszloteveli.Context()): EvaluatedType?
}

class TypedInterpreter(private val dataTypes: List<com.laszloteveli.DataType<*>> = listOf(),
                       private val functions: List<Pattern<*, TypedInterpreter>> = listOf(),
                       override var context: com.laszloteveli.Context = com.laszloteveli.Context()
) : Interpreter<Any> {
    private val dataTypeCache: MutableMap<String, com.laszloteveli.DataType<*>> = mutableMapOf()
    private val functionsCache: MutableMap<String, Pattern<*, TypedInterpreter>> = mutableMapOf()

    override val interpreterForEvaluatingVariables: Interpreter<*>
        get() { return this }

    override fun evaluate(expression: String): Any = evaluate(expression, com.laszloteveli.Context())
    override fun evaluateOrNull(expression: String): Any? = evaluateOrNull(expression, com.laszloteveli.Context())

    override fun evaluate(expression: String, context: com.laszloteveli.Context): Any = evaluateOrNull(expression, context) as Any
    override fun evaluateOrNull(expression: String, context: com.laszloteveli.Context): Any? {
        context.merge(this.context)
        val input = expression.trim()
        val connectedRanges = collectConnectedRanges(input, functions)
        return functionFromCache(input, context, connectedRanges)
            ?: dataTypeFromCache(input)
            ?: dataType(input)
            ?: variable(input, context)
            ?: function(input, context, connectedRanges)
    }

    override fun print(input: Any): String {
        return dataTypes
                .asSequence()
                .mapNotNull { it.print(input) }
                .firstOrNull()
            ?: input.toString()
    }

    private fun dataType(expression: String, interpreter: TypedInterpreter = this) =
        find(dataTypes, { it.convert(expression, interpreter) }, { dataTypeCache[expression] = it })

    private fun function(expression: String, context: com.laszloteveli.Context, connectedRanges: List<IntRange>, interpreter: TypedInterpreter = this) =
        find(functions.reversed(), { it.convert(expression, interpreter, context, connectedRanges) }, { functionsCache[expression] = it })

    private fun variable(expression: String, context: com.laszloteveli.Context): Any? =
        find(context.variables, expression) { it }

    private fun dataTypeFromCache(expression: String, interpreter: TypedInterpreter = this) =
        find(dataTypeCache, expression) { it.convert(expression, interpreter) }

    private fun functionFromCache(expression: String, context: com.laszloteveli.Context, connectedRanges: List<IntRange>, interpreter: TypedInterpreter = this) =
        find(functionsCache, expression) { it.convert(expression, interpreter, context, connectedRanges) }

    private fun <T, R> find(source: List<T>, match: (T) -> R, cache: (T) -> Unit) = source
        .asSequence()
        .map { it to match(it) }
        .filter { it.second != null }
        .firstOrNull()
        .alsoIfNotNull { cache(it.first) }
        ?.second

    private fun <T, R> find(source: Map<String, T>, key: String, call: (T) -> R) = source
        .entries
        .firstOrNull { it.key == key }
        ?.run { call(value) }
}

inline fun <T> T?.alsoIfNotNull(block: (T) -> Unit): T? = also { if (it != null) block(it) }
