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

data class VariableOptions(val interpreted : Boolean = true,
                           val trimmed : Boolean = true,
                           val exhaustive : Boolean = false,
                           val acceptsNullValue : Boolean = false) {
    class Builder(var interpreted: Boolean,
                  var trimmed : Boolean,
                  var exhaustive : Boolean,
                  var acceptsNullValue : Boolean) {
        fun build() = VariableOptions(interpreted, trimmed, exhaustive, acceptsNullValue)
    }
    fun builder(): Builder = Builder(interpreted, trimmed, exhaustive, acceptsNullValue)
}

open class Variable<T>(
    val name: String,
    val options: VariableOptions = VariableOptions(),
    val map: (input: Any, interpreter: Interpreter<*>) -> T? = { input, _ -> input as? T }
): PatternElement {
    override fun matches(prefix: String, options: PatternOptions): MatchResult {
        return MatchResult.AnyMatch(this.options.exhaustive)
    }

    class Builder<T>(var name: String,
                     var options: VariableOptions = VariableOptions(),
                     var map: (input: Any, interpreter: Interpreter<*>) -> T?) {
        fun build() = Variable(name, options, map)
    }
    fun builder(): Builder<T> = Builder(name, options, map)

    fun copy(options: VariableOptions = this.options): Variable<T> = Variable(name, options, map)
}

class TemplateVariable(
    name: String,
    options: VariableOptions = VariableOptions(),
    map: (input: String, interpreter: StringTemplateInterpreter) -> String? = { value, _ -> value }
) : Variable<String>(name, options.copy(interpreted = false), { value, interpreter ->
    val stringValue = value as? String ?: ""
    val stringInterpreter = interpreter as StringTemplateInterpreter
    val result = if (options.interpreted) stringInterpreter.evaluate(stringValue) else stringValue
    map(result, stringInterpreter)
})
