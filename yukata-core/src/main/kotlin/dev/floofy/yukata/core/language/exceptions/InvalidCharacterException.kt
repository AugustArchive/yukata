package dev.floofy.yukata.core.language.exceptions

/**
 * Exception of when the [Lexer][dev.floofy.yukata.core.language.Lexer] has received
 * a invalid character in the GraphQL schema.
 */
class InvalidCharacterException(message: String): Exception(message)
