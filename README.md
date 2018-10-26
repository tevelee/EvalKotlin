# { Eval }

[![Travis CI status](https://travis-ci.org/tevelee/EvalKotlin.svg?branch=master)](https://travis-ci.org/tevelee/EvalKotlin)
[![Framework version](https://img.shields.io/badge/Version-1.0.0-yellow.svg)]()
[![Kotlin version](https://img.shields.io/badge/Kotlin-1.3-orange.svg)]()
[![Platforms](https://img.shields.io/badge/Platforms-JVM%20|%20JS%20|%20Native%20(iOS)-blue.svg)]()
[![Lincese](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://github.com/tevelee/Eval/tree/master/LICENSE.txt)

---

- [👨🏻‍💻 About](#-about)
- [📈 Getting Started](#-getting-started)
	- [🤓 Short Example](#-short-example)
	- [⚡️ Installation](#%EF%B8%8F-installation)
	- [⁉️ How does it work?](#%EF%B8%8F-how-does-it-work)
- [🏃🏻 Status](#-status)
- [💡 Motivation](#-motivation)
- [📚 Examples](#-examples)
- [🙋 Contribution](#-contribution)
- [👀 Details](#-details)
- [👤 Author](#-author)
- [⚖️ License](#%EF%B8%8F-license)

## 👨🏻‍💻 About

**Eval** is a lightweight interpreter framework written in <img src="https://upload.wikimedia.org/wikipedia/commons/7/74/Kotlin-logo.svg" width="16"> Kotlin, supporting JVM, JS and now Native (📱iOS) platforms.

Created based on the Swift equivalent [Eval](https://github.com/tevelee/Eval/blob/master/README.md) project.

It evaluates expressions at runtime, with operators and data types you define.

🍏 Pros | 🍎 Cons
------- | --------
🐥 Lightweight - the whole engine is really just a few hundred lines of code | 🤓 Creating custom operators and data types, on the other hand, can take a few extra lines - depending on your needs
✅ Easy to use API - create new language elements in just a matter of seconds | ♻️ The evaluated result of the expressions must be strongly typed, so you can only accept what type you expect the result is going to be
🎢 Fun - Since it is really easy to play with, it's joyful to add - even complex - language features | -
🚀 Fast execution - I'm trying to optimise as much as possible. Has its limitations though | 🌧 Since it is a really generic concept, some optimisations cannot be made, compared to native interpreters

The framework currently supports two different types of execution modes:

- **Strongly typed expressions**: like a programming language
- **Template languages**: evaluating expressions in arbitrary string environments

*Let's see just a few examples:*

It's extremely easy to formulate expressions (and evaluate them at runtime), like 

- `5 in 1...3` evaluates to `false` Bool type
- `'Eval' starts with 'E'` evaluates to `true` Bool type
- `'b' in ['a','c','d']` evaluates to `false` Bool type
- `x < 2 ? 'a' : 'b'` evaluates to `"a"` or `"b"` String type, based on the `x` Int input variable
- `Date(2018, 12, 13).format('yyyy-MM-dd')` evaluates to `"2018-12-13"` string
- `'hello'.length` evaluates to `5` Integer
- `now` evaluates to `Date()`

And templates, such as

- `{% if name != nil %}Hello{% else %}Bye{% endif %} {{ name|default('user') }}!`, whose output is `Hello Adam!` or `Bye User!`
- `Sequence: {% for i in 1...5 %}{{ 2 * i }} {% endfor %}` which is `2 4 6 8 10 `

And so on... The result of these expressions depends on the content, determined by the evaluation. It can be any type which is returned by the functions (String, List<Double>, Date, or even custom types of your own.)

You can find various ways of usage in the examples section below.

## 🏃🏻 Status

- [x] Library implementation
- [ ] API finalisation
- [ ] Package Manager support
- [x] Initial documentation
- [ ] Example project (template engine)
- [ ] CI
- [ ] Code test-coverage
- [ ] v1.0
- [x] Contribution guides

This is a really early stage of the project, I'm still deep in the process of all the open-sourcing related tasks, such as firing up a CI, creating a beautiful documentation page, managing administrative tasks around stability. 

Please stay tuned for the updates!

## 📈 Getting started

For the expressions to work, you'll need to create an interpreter instance, providing your data types and expressions you aim to support, and maybe some input variables - if you need any.

```kotlin
val interpreter = TypedInterpreter(dataTypes = listOf(integer, string, boolean),
                                   functions = listOf(multiplication, addition, ternary),
                                   context = Context(variables = mapOf("x" to 1)))
```

And call it with a string expression, as follows.

```kotlin
val value = interpreter.evaluate("2 * x + 1") as? Int
```

### 🤓 Short example

Let's check out a fairly complex example, and build it from scratch! Let's implement a language which can parse the following expression:

```kotlin
x != 0 ? 5 * x : pi + 1
```

There's a ternary operator `?:` in there, which we will need. Also, supporting number literals (`0`, `5`, and `1`) and boolean types (`true/false`). There's also a not equal operator `!=` and a `pi` constant. Let's not forget about the addition `+` and multiplication `*` as well!

First, here are the data types.

```kotlin
val numberLiteral = Literal { value, _ -> value.toDoubleOrNull() }  //Converts every number literal, if it can be represented with a Double instance
val piConstant = Literal("pi") { kotlin.math.PI }

val number = DataType(listOf(numberLiteral, piConstant))
```

```kotlin
val trueLiteral = Literal("true") { true }
val falseLiteral = Literal("false") { false }

val boolean = DataType(listOf(trueLiteral, falseLiteral)) { if (it) "true" else "false" }
```

(The last parameter, expressed as a block, tells the framework how to formulise this type of data as a String for debug messages or other purposes)

Now, let's build the operators:

```kotlin
val multiplication = Pattern<Double, TypedInterpreter>(Variable<Double>("lhs") + "*" + Variable<Double>("rhs")) {
    val lhs = variables["lhs"] as? Double ?: return@Pattern null
    val rhs = variables["rhs"] as? Double ?: return@Pattern null
    lhs * rhs
}
```
```kotlin
val addition = Pattern<Double, TypedInterpreter>(Variable<Double>("lhs") + "+" + Variable<Double>("rhs")) {
    val lhs = variables["lhs"] as? Double ?: return@Pattern null
    val rhs = variables["rhs"] as? Double ?: return@Pattern null
    lhs + rhs
}
```
```kotlin
val notEquals = Pattern<Boolean, TypedInterpreter>(Variable<Double>("lhs") + "!=" + Variable<Double>("rhs")) {
    val lhs = variables["lhs"] as? Double ?: return@Pattern null
    val rhs = variables["rhs"] as? Double ?: return@Pattern null
    lhs != rhs
}
```
```kotlin
val ternary = Pattern<Any, TypedInterpreter>(Variable<Boolean>("condition") + "?" + Variable<Any>("true") + ":" + Variable<Any>("false")) {
    val condition = variables["condition"] as? Boolean ?: return@Pattern null
    if (condition) variables["true"] else variables["false"]
}
```

Looks like, we're all set. Let's evaluate our expression!

```kotlin
val interpreter = TypedInterpreter(dataTypes = listOf(number, boolean),
                                   functions = listOf(multipication, addition, notEquals, ternary))
                                                                      
val result = interpreter.evaluate("x != 0 ? 5 * x : pi + 1", Context(mapOf("x" to 3)))

assertEquals(result, 15)
```

Now, that we have operators and data types, we can also evaluate anything using these data types:

* `interpreter.evaluate("3 != 4") as Bool`
* `interpreter.evaluate("2 + 1.5 * 6") as Double` (since multiplication is defined earlier in the array, it has a higher precedence, as expected)
* `interpreter.evaluate("true ? 1 : 2.5") as Double`

As you have seen, it's really easy and intuitive to build custom languages, using simple building blocks. With just a few custom data types and functions, the possibilities are endless. Operators, functions, string, arrays, dates...

The motto of the framework: Build your own (mini) language!

### ⚡️ Installation

TBD

### ⁉️ How does it work?

The interpreter itself does not define anything or any way to deal with the input string on its own. 
All it does is recognising patterns. 

By creating data types, you provide literals to the framework, which it can interpret as an element or a result of the expression. 
These types are transformed to real Kotlin types.

By defining functions, you provide patterns to the framework to recognise. 
Functions are also typed, they return Kotlin types as a result of their evaluation.
Functions consist of keywords and variables, nothing more. 

- Keywords are static strings which should not be interpreted as data (such as `if`, or `{`, `}`). 
- Variables, on the other hand, are typed values, recursively evaluated. For example, if a variable recognises something, that proves to be a further pattern, it recursively evaluates their body, until they find context-variables or literals of any given data type.

Functions also have blocks, which provide the recognised variables in a key-value dictionary parameter, and you can do whatever you want with them: print them, convert them, modify or assign them to context-variables. 

The addition function above, for example, consists of two variables on each side, and the `+` keyword in the middle. It also requires a block, where both sides are given in a `Map<String, Any>`, so the closure can get the values of the placeholders and add them together.

There's one interesting aspect of this solution: Unlike traditional - native - interpreters or compilers, this one recognises patterns from top to bottom. 
Meaning, that it looks at the input string, your expression, and recognises patterns in priority order, and recursively go deeper and deeper until the most basic expressions are met.

A traditional interpreter, however, parses expressions character by character, feeding the results to a lexer, the tokeniser, then builds up an abstract syntax tree (which is highly optimisable), and finally converts it to a binary (compiler) or evaluates it at runtime (interpreter), in one word: bottom-up.

The two solutions can be compared in various ways. The two main differences are in ease of use, and performance. 
This version of an interpreter provides an effortless way to define patterns, types, etc., but has its cost! It cannot parse as optimally as a traditional compiler could, as it doesn't have an internal graph of expressions (AST), but still performs in a much more than acceptable way.
Definition-wise, this framework provides an easily understandable way of language-elements, but the traditional one really lacks behind, because the lexer is usually an ugly, hardly understandable state machine, or regular expression, BAKED INTO the interpreter code itself.

## 💡 Motivation

I have another project, in which I'm generating Objective-C and Swift model objects with loads of utils, based on really short templates. This project was not possible currently in Swift, as there is no template language - capable enough - to create my templates. (I ended up using a third party PHP framework, called [Twig](https://github.com/twigphp/Twig)). So finally, I created one for [Swift](https://github.com/tevelee/Eval). And now in Kotlin!

It turned out, that making it a little more generic - here and there - makes the whole thing really capable and flexible of using in different use-cases.

The pattern matching was there, but soon I realised, that I'm going to need expressions as well, for printing, evaluating in if/while statements and so on. First, I was looking at an excellent library, [Expression](https://github.com/nicklockwood/Expression), created by Nick Lockwood, which is capable of evaluating numeric expressions. Unfortunately, I wanted a bit more, defining strings, dates, array, and further types and expressions, so I used my existing pattern matching solution to bring this capability to life.

It ended up quite positively after I discovered the capabilities of a generic solution like this. The whole thing just blew my mind, language features could have been defined in a matter of seconds, and I wanted to share this discovery with the world, so here you are :)

## 📚 Examples
​
TBD

## 🙋 Contribution

Anyone is more than welcome to contribute to **Eval**! It can even be an addition to the docs or to the code directly, by [raising an issue](https://github.com/tevelee/EvalKotlin/issues/new) or in the form of a pull request. Both are equally valuable to me! Happy to assist anyone!

In case you need help or want to report a bug - please file an issue. Make sure to provide as much information as you can; sample code also makes it a lot easier for me to help you. Check out the [contribution guidelines](https://github.com/tevelee/EvalKotlin/tree/master/CONTRIBUTING.md) for further information. 

I collected some use cases, and great opportunities for beginner tasks if anybody is motivated to bring this project to a more impressive state!

## 👀 Details

Please check out [https://tevelee.github.io/EvalKotlin](https://tevelee.github.io/EvalKotlin) for the more detailed documentation pages!

## 👤 Author

I am Laszlo Teveli, software engineer. In my free time I like to work on my hobby projects and open sourcing them 😉

Feel free to reach out to me anytime via `tevelee [at] gmail [dot] com`, or `@tevelee` on Twitter.

## ⚖️ License

**Eval** is available under the Apache 2.0 licensing rules. See the [LICENSE](https://github.com/tevelee/EvalKotlin/tree/master/LICENSE.txt) file for more information.
