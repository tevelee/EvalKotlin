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

class ExpressionInfo(val input: String,
                     val output: Any,
                     val patterns: String,
                     val variables: Map<String, Any>) {
    override fun toString(): String = "(" +
            "input=$input, " +
            "output=$output, " +
            "patterns=$patterns, " +
            "variables:$variables" +
            ")"
}

class ExpressionStack(val variables: Map<String, Any>,
                      val debugInfo: Map<String, com.laszloteveli.ExpressionInfo>)

class Context(var variables: MutableMap<String, Any> = mutableMapOf(),
              var debugInfo: MutableMap<String, com.laszloteveli.ExpressionInfo> = mutableMapOf(),
              val stack: MutableList<com.laszloteveli.ExpressionStack> = mutableListOf()) {
    fun merge(context: com.laszloteveli.Context) {
        variables.putAll(context.variables)
        debugInfo.putAll(context.debugInfo)
    }

    fun push() = stack.add(com.laszloteveli.ExpressionStack(variables, debugInfo))
    fun pop() = stack.removeAt(stack.lastIndex).let {
        debugInfo = it.debugInfo.toMutableMap()
        variables = it.variables.toMutableMap()
    }
}
