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

import com.laszloteveli.MatchResult.*

open class TemplateInterpreter<T>(open val statements: List<Pattern<T, TemplateInterpreter<T>>> = listOf(),
                                  open val interpreter: TypedInterpreter = TypedInterpreter(),
                                  override val context: com.laszloteveli.Context
) : Interpreter<T> {
    override val interpreterForEvaluatingVariables: Interpreter<*>
        get() { return interpreter }

    override fun evaluate(expression: String): T = evaluate(expression, com.laszloteveli.Context())
    override fun evaluateOrNull(expression: String): T? = evaluateOrNull(expression, com.laszloteveli.Context())

    override fun evaluate(expression: String, context: com.laszloteveli.Context): T = evaluateOrNull(expression, context) as T
    override fun evaluateOrNull(expression: String, context: com.laszloteveli.Context): T? =
        error("Shouldn't instantiate `TemplateInterpreter` directly. Please subclass with a dedicated type instead")

    fun evaluate(expression: String, context: com.laszloteveli.Context, reducer: TemplateReducer<T>): T?{
        context.merge(this.context)
        var output = reducer.initialValue

        var position = 0
        do {
            val result = matchStatement(statements, expression, this, context, position)
            when (result) {
                is NoMatch, is PossibleMatch -> {
                    output = reducer.reduceCharacter(output, expression[position])
                    position += 1
                }
                is ExactMatch<*> -> {
                    output = reducer.reduceValue(output, result.output as T)
                    position += result.length
                }
                else -> error("Invalid result")
            }
        } while (position < expression.length)

        return output
    }

    override fun print(input: Any): String = interpreter.print(input)
}

class StringTemplateInterpreter(
    override val statements: List<Pattern<String, TemplateInterpreter<String>>> = listOf(),
    override val interpreter: TypedInterpreter = TypedInterpreter(),
    override val context: com.laszloteveli.Context = com.laszloteveli.Context()
) : TemplateInterpreter<String>(statements, interpreter, context) {
    override fun evaluateOrNull(expression: String, context: com.laszloteveli.Context): String? {
        val reducer = TemplateReducer("",
            { a, b -> a + b },
            { a, b -> a + b })
        return evaluate(expression, context, reducer)
    }
}