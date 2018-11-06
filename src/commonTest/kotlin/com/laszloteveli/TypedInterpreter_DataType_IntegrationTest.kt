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
import kotlin.test.assertNull

class TypedInterpreter_DataType_IntegrationTest {
    @Test
    fun whenEvaluatedWithPlainString_thenReturnsTheSameTypedValue() {
        val stringLiteral = Literal { input, _ -> input }
        val stringType = com.laszloteveli.DataType(listOf(stringLiteral))
        val interpreter = TypedInterpreter(listOf(stringType))

        val value = interpreter.evaluate("test")

        assertEquals(value, "test")
    }

    @Test
    fun whenEvaluatedWithPlainInteger_thenReturnsTheSameTypedValue() {
        val integerLiteral = Literal { input, _ -> input.toIntOrNull() }
        val integerType = com.laszloteveli.DataType(listOf(integerLiteral))
        val interpreter = TypedInterpreter(listOf(integerType))

        val value = interpreter.evaluate("2")

        assertEquals(value, 2)
    }

    @Test
    fun whenEvaluatedWithBooleanTrue_thenReturnsTheSameTypedValue() {
        val trueLiteral = Literal("true") { true }
        val booleanType = com.laszloteveli.DataType(listOf(trueLiteral))
        val interpreter = TypedInterpreter(listOf(booleanType))

        val value = interpreter.evaluate("true")

        assertEquals(value, true)
    }

    @Test
    fun whenEvaluatedWithBooleanFalse_thenReturnsTheSameTypedValue() {
        val falseLiteral = Literal("false") { false }
        val booleanType = com.laszloteveli.DataType(listOf(falseLiteral))
        val interpreter = TypedInterpreter(listOf(booleanType))

        val value = interpreter.evaluate("false")

        assertEquals(value, false)
    }

    @Test
    fun whenEvaluatedWithNoTypes_thenReturnsNull() {
        val interpreter = TypedInterpreter(listOf())

        val value = interpreter.evaluateOrNull("anything")

        assertNull(value)
    }

    @Test
    fun whenEvaluatedWithMultipleTypes_thenAllParseCorrectly() {
        val stringLiteral = Literal { input, _ -> input }
        val stringType = com.laszloteveli.DataType(listOf(stringLiteral))

        val integerLiteral = Literal { input, _ -> input.toIntOrNull() }
        val integerType = com.laszloteveli.DataType(listOf(integerLiteral))

        val trueLiteral = Literal("true") { true }
        val booleanType = com.laszloteveli.DataType(listOf(trueLiteral))

        val interpreter = TypedInterpreter(listOf(booleanType, integerType, stringType))

        val value1 = interpreter.evaluate("true")
        val value2 = interpreter.evaluate("5")
        val value3 = interpreter.evaluate("teve")

        assertEquals(value1, true)
        assertEquals(value2, 5)
        assertEquals(value3, "teve")
    }
}