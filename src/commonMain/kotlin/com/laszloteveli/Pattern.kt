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

interface PatternElement {
    fun matches(prefix: String, options: PatternOptions): MatchResult
}

data class PatternOptions(val backwardMatch: Boolean = false)
data class PatternBody<E>(val variables: Map<String, Any>, val evaluator: E, val context: com.laszloteveli.Context)

class Pattern<T, I: Interpreter<*>>(elements: List<PatternElement>,
                                    private val options: PatternOptions = PatternOptions(),
                                    val matcher: PatternBody<I>.() -> T?) {
    val elements = replaceLastElementNotToBeShortestMatch(elements)

    private fun replaceLastElementNotToBeShortestMatch(elements: List<PatternElement>): List<PatternElement> {
        val index = if (options.backwardMatch) 0 else elements.lastIndex
        val variable = elements[index] as? Variable<*> ?: return elements
        val newVariable= variable.copy(options = variable.options.copy(exhaustive = true))
        return elements.replace(index, newVariable)
    }

    fun matches(string: String,
                startIndex: Int = 0,
                interpreter: I,
                context: com.laszloteveli.Context,
                connectedRanges: List<IntRange>): MatchResult {
        val variableProcessor = VariableProcessor(interpreter, context)
        val matcher = Matcher<T>(elements, options, variableProcessor)
        val result = matcher.match(string, startIndex, connectedRanges) {
            PatternBody(it, interpreter, context).matcher()
        }
        if (result is MatchResult.ExactMatch<*>) {
            context.debugInfo[string] =
                    com.laszloteveli.ExpressionInfo(string, result.output!!, pattern(), result.variables)
        }
        return result
    }

    private fun pattern(): String {
        return elements.map {
            return when (it) {
                is Keyword -> it.name
                is Variable<*> -> "{${it.name}}"
                else -> ""
            }
        }.joinToString(" ")
    }
}
