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

import com.laszloteveli.Keyword.Type.*
import com.laszloteveli.MatchResult.*

data class ActiveVariable(val name: String, var value: String, val metadata: Variable<*>)

class Matcher<T>(
    private val elements: List<PatternElement>,
    private val options: PatternOptions,
    private val processor: VariableProcessor
) {
    fun match(string: String,
              startIndex: Int = 0,
              connectedRanges: List<IntRange>,
              renderer: (variables: Map<String, Any>) -> T?): MatchResult {
        var currentlyActiveVariable: ActiveVariable? = null
        var elementIndex = initialIndex()
        val trimmed = string.substring(startIndex = startIndex)
        var remainder = trimmed
        val variables: MutableMap<String, Any> = mutableMapOf()
        do {
            val element = elements[elementIndex]
            val result = element.matches(remainder, options)
            when (result) {
                is NoMatch -> {
                    currentlyActiveVariable
                        ?: return NoMatch
                    remainder = proceed(currentlyActiveVariable, remainder)
                }
                is PossibleMatch ->
                    return PossibleMatch
                is AnyMatch -> {
                    if (currentlyActiveVariable == null && element is Variable<*>)
                        currentlyActiveVariable = ActiveVariable(element.name, String(), element)
                    if (result.exhaustive) {
                        if (currentlyActiveVariable != null)
                            remainder = proceed(currentlyActiveVariable, remainder)
                        if (remainder.isEmpty()) {
                            registerVariable(currentlyActiveVariable, variables)
                                ?: return PossibleMatch
                            elementIndex = nextElement(elementIndex)
                        }
                    } else
                        elementIndex = nextElement(elementIndex)
                }
                is ExactMatch<*> -> {
                    val position =
                        if (options.backwardMatch) remainder.length
                        else trimmed.length - remainder.length
                    val isOpeningOrClosingKeyword = element is Keyword && element.type != GENERIC
                    if (isEmbedded(element, string.substring(startIndex = startIndex), position)) {
                        if (currentlyActiveVariable != null)
                            remainder = proceed(currentlyActiveVariable, remainder)
                        else
                            elementIndex = nextElement(elementIndex)
                    } else if (connectedRanges.any { it.contains(position) } && !isOpeningOrClosingKeyword) {
                        if (currentlyActiveVariable != null)
                            remainder = proceed(currentlyActiveVariable, remainder)
                    } else {
                        variables.putAll(result.variables)
                        registerVariable(currentlyActiveVariable, variables)
                        currentlyActiveVariable = null
                        elementIndex = nextElement(elementIndex)
                        remainder = drop(remainder, result.length)
                        if (elementIndex < elements.lastIndex && element is Keyword) {
                            remainder = trim(remainder)
                        }
                    }
                }
            }
        } while (notFinished(elementIndex))

        val output = renderer(variables)
        return if (output != null)
            ExactMatch(string.length - startIndex - remainder.length, output, variables)
        else
            NoMatch
    }

    private fun isEmbedded(element: PatternElement, input: String, position: Int): Boolean {
        if (element is Keyword && element.type == CLOSING_TAG) {
            val closingPosition = closingPosition(input)
                ?: return false
            return position < closingPosition
        }
        return false
    }

    private fun closingPosition(input: String, startIndex: Int = 0): Int? {
        val opening = elements.firstOrNull { it is Keyword && it.type == OPENING_TAG } as? Keyword
                ?: return null
        val closing = elements.firstOrNull { it is Keyword && it.type == CLOSING_TAG } as? Keyword
                ?: return null
        var counter = 0
        var position = startIndex
        do {
            val openingIndex = input.indexOf(opening.name, position)
            val closingIndex = input.indexOf(closing.name, position)
            val didFindOpeningTag = openingIndex != -1
            val didFindClosingTag = closingIndex != -1
            val isCloseTagEarlier = didFindOpeningTag && didFindClosingTag
                    && closingIndex < openingIndex

            if (didFindOpeningTag && !isCloseTagEarlier) {
                position = openingIndex + opening.name.length
                counter++
            } else if (didFindClosingTag) {
                position = closingIndex + closing.name.length
                counter--
                if (counter == 0)
                    return closingIndex
            } else {
                break
            }
        } while (true)
        return null
    }

    private fun initialIndex(): Int = if (options.backwardMatch) elements.lastIndex else 0
    private fun nextElement(elementIndex: Int): Int =
        if (options.backwardMatch) elementIndex - 1
        else elementIndex + 1
    private fun drop(remainder: String, length: Int) =
        if (options.backwardMatch) remainder.dropLast(length)
        else remainder.drop(length)

    private fun notFinished(elementIndex: Int): Boolean =
        if (options.backwardMatch) elementIndex >= 0 else elementIndex <= elements.lastIndex

    private fun trim(remainder: String): String =
        if (options.backwardMatch) remainder.trimEnd() else remainder.trimStart()

    private fun appendNextCharacterToVariable(currentlyActiveVariable: ActiveVariable, remainder: String) {
        if (options.backwardMatch) {
            currentlyActiveVariable.value = remainder.last() + currentlyActiveVariable.value
        } else {
            currentlyActiveVariable.value += remainder.first()
        }
    }

    private fun proceed(currentlyActiveVariable: ActiveVariable, remainder: String): String {
        appendNextCharacterToVariable(currentlyActiveVariable, remainder)
        return drop(remainder, 1)
    }

    private fun registerVariable(
        currentlyActiveVariable: ActiveVariable?,
        variables: MutableMap<String, Any>
    ): Boolean? {
        if (currentlyActiveVariable != null) {
            val value = processor.process(currentlyActiveVariable)
            value?.let { variables[currentlyActiveVariable.name] = it }
            return if (value == null) currentlyActiveVariable.metadata.options.acceptsNullValue else false
        }
        return null
    }
}