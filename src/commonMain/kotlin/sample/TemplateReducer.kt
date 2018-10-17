package sample

typealias Reducer<T, K> = (existing: T, next: K) -> T

class TemplateReducer<T>(val initialValue: T,
                         val reduceValue: Reducer<T, T>,
                         val reduceCharacter: Reducer<T, Char>)