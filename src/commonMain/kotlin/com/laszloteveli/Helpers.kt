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

fun <T> List<T>.transform(block: MutableList<T>.() -> Unit): List<T> = toMutableList().apply { this.block() }
fun <T> List<T>.replace(index: Int, item: T): List<T> = transform {
    removeAt(index)
    add(index, item)
}
inline fun <T> T.applyIf(condition: Boolean, transform: (T) -> T): T = if (condition) transform(this) else this

operator fun PatternElement.plus(other: PatternElement) = listOf(this, other)
operator fun List<PatternElement>.plus(other: PatternElement) = transform { add(other) }

operator fun String.plus(other: PatternElement) = Keyword(this) + other
operator fun PatternElement.plus(other: String) = this + Keyword(other)
operator fun List<PatternElement>.plus(other: String) = this + Keyword(other)
