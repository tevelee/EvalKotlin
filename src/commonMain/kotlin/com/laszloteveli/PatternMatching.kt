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

import kotlin.math.min
import com.laszloteveli.Keyword.Type.*
import com.laszloteveli.MatchResult.*

fun <T> Pattern<T, TypedInterpreter>.convert(input: String,
                                             interpreter: TypedInterpreter,
                                             context: com.laszloteveli.Context,
                                             connectedRanges: List<IntRange>): Any? {
    val result = matchStatement(listOf(this), input, interpreter, context, 0, connectedRanges)
    return when (result) {
        is ExactMatch<*> -> result.output
        else -> null
    }
}

data class MatchStatementResult<T, I : Interpreter<*>>(val element: Pattern<T, I>, val result: MatchResult)

fun <T, I : Interpreter<*>> matchStatement(statements: List<Pattern<T, I>>,
                                           input: String,
                                           interpreter: I,
                                           context: com.laszloteveli.Context,
                                           startIndex: Int = 0,
                                           connectedRanges: List<IntRange> = listOf()): MatchResult {
    val results = statements.asSequence().map {
        MatchStatementResult(it, it.matches(input, startIndex, interpreter, context, connectedRanges))
    }
    val matchingElement = results.firstOrNull { it.result is ExactMatch<*> }
    return when {
        matchingElement != null -> matchingElement.result
        results.any { it.result is PossibleMatch } -> PossibleMatch
        else -> NoMatch
    }
}

internal fun collectConnectedRanges(input: String, statements: List<Pattern<*, *>>): List<IntRange> =
    statements.mapNotNull { pattern ->
        val keywords = pattern.elements.map { it as? Keyword }.filterNotNull()
        val openingKeywords = keywords.filter { it.type == OPENING_TAG }.map { it.name }
        val closingKeywords = keywords.filter { it.type == CLOSING_TAG }.map { it.name }

        if (openingKeywords.isEmpty() && closingKeywords.isEmpty()) return@mapNotNull null

        val ranges = mutableListOf<IntRange>()
        val rangeStart = mutableListOf<Int>()
        var position = 0
        do {
            val start = input.indexOfAny(openingKeywords, startIndex = position)
            val end = input.indexOfAny(closingKeywords, startIndex = position)
            if (start != -1 && end != -1) {
                if (start < end) {
                    rangeStart.add(start)
                } else {
                    val lastOpening = rangeStart.removeAt(rangeStart.lastIndex)
                    ranges.add(IntRange(lastOpening, end))
                }
                position = min(start, end) + 1
            } else if (start != -1) {
                rangeStart.add(start)
                position = start + 1
            } else if (end != -1) {
                if (rangeStart.isEmpty()) return@mapNotNull null
                val lastOpening = rangeStart.removeAt(rangeStart.lastIndex)
                ranges.add(IntRange(lastOpening, end))
                position = end + 1
            } else {
                break;
            }
        } while(position < input.length)

        if (ranges.isEmpty()) null else ranges
    }.fold(mutableListOf()) { acc, item -> acc.apply { addAll(item) } }