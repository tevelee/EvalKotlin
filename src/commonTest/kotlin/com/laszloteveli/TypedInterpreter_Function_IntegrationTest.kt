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

import kotlin.test.Test
import kotlin.test.assertEquals
import com.laszloteveli.Keyword.Type.*

class TypedInterpreter_Pattern_IntegrationTest {
    @Test
    fun whenEvaluatedWithPattern_thenReturnsTheComputedValue() {
        val string = com.laszloteveli.DataType(listOf(
            Literal("a") { "a" },
            Literal("b") { "b" }
        ))
        val interpreter = TypedInterpreter(listOf(string), listOf(concatOperator()))

        val value = interpreter.evaluate("a + b")

        assertEquals( "ab", value)
    }

    @Test
    fun whenEvaluatedWithPatternWithMultipleUse_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = com.laszloteveli.DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(multiplyOperator()))

        val value = interpreter.evaluate("1 * 2 * 3")

        assertEquals( 6, value)
    }

    @Test
    fun whenEvaluatedWithPatternBackwards_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = com.laszloteveli.DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(multiplyOperator(), addOperator()))

        val value = interpreter.evaluate("2 + 3 * 4")

        assertEquals( 14, value)
    }

    @Test
    fun whenEvaluatedWithNonCommutativePatternBackwards_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = com.laszloteveli.DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(subtractOperator()))

        val value = interpreter.evaluate("6 - 4 - 2")

        assertEquals( 0, value)
    }

    @Test
    fun whenEvaluatedWithNonCommutativePatternWithParentheses_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = com.laszloteveli.DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(parentheses(), subtractOperator(), addOperator()))

        val value = interpreter.evaluate("6 - (4 + 2)")

        assertEquals( 0, value)
    }

    @Test
    fun whenEvaluatedWithPatternWithSimpleParentheses_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = com.laszloteveli.DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(parentheses(), addOperator()))

        val value = interpreter.evaluate("(2 + 3)")

        assertEquals( 5, value)
    }

    @Test
    fun whenEvaluatedWithPatternWithParentheses_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = com.laszloteveli.DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(parentheses(), multiplyOperator(), addOperator()))

        val value = interpreter.evaluate("(2 + 3) * 4")

        assertEquals( 20, value)
    }

    @Test
    fun whenEvaluatedWithMultipleOperators_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = com.laszloteveli.DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(parentheses(), subtractOperator()))

        val value = interpreter.evaluate("6 - (4 - 2)")

        assertEquals( 4, value)
    }

    @Test
    fun whenEvaluatedWithPatternWithEmbeddedParentheses_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = com.laszloteveli.DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(parentheses(), subtractOperator()))

        val value = interpreter.evaluate("12 - (6 - (4 - 2))")

        assertEquals( 8, value)
    }

    @Test
    fun whenEvaluatedWithContextVariables_thenReturnsTheComputedValue() {
        val literal = Literal { value, _ -> value.toIntOrNull() }
        val integer = com.laszloteveli.DataType(listOf(literal))
        val interpreter = TypedInterpreter(listOf(integer), listOf(multiplyOperator()),
            com.laszloteveli.Context(mutableMapOf("y" to 2))
        )

        val value = interpreter.evaluate("x * y * 3", com.laszloteveli.Context(mutableMapOf("x" to 6)))

        assertEquals( 36, value)
    }

    private fun concatOperator(): Pattern<String, TypedInterpreter> = infixOperator("+") { a: String, b: String -> a + b }
    private fun multiplyOperator(): Pattern<Int, TypedInterpreter> = infixOperator("*") { a: Int, b: Int -> a * b }
    private fun addOperator(): Pattern<Int, TypedInterpreter> = infixOperator("+") { a: Int, b: Int -> a + b }
    private fun subtractOperator(): Pattern<Int, TypedInterpreter> = infixOperator("-") { a: Int, b: Int -> a - b }

    private fun <L, R, T> infixOperator(symbol: String, reduce: (L, R) -> T): Pattern<T, TypedInterpreter> = Pattern(Variable<L>("lhs") + symbol + Variable<R>("rhs"), PatternOptions(backwardMatch = true)) {
        val lhs = variables["lhs"] as? L ?: return@Pattern null
        val rhs = variables["rhs"] as? R ?: return@Pattern null
        reduce(lhs, rhs)
    }

    private fun parentheses(): Pattern<Any, TypedInterpreter> = Pattern(Keyword("(", OPENING_TAG) + Variable<Int>("body") + Keyword(")", CLOSING_TAG)) {
        variables["body"]
    }
}