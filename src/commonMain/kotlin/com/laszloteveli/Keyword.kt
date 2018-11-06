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

class Keyword(val name: String,
              val type: Type = Type.GENERIC): PatternElement {
    enum class Type {
        GENERIC,
        OPENING_TAG,
        CLOSING_TAG
    }

    override fun matches(prefix: String, options: PatternOptions): MatchResult {
        val checker: (String, String) -> Boolean =
            if (options.backwardMatch) { a, b -> a.endsWith(b) }
            else { a, b -> a.startsWith(b) }
        return when {
            name == prefix || checker.invoke(prefix, name) -> ExactMatch(name.length, name, mapOf())
            checker.invoke(name, prefix) -> PossibleMatch
            else -> NoMatch
        }
    }
}