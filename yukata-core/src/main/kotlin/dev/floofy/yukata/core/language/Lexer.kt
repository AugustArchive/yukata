/**
 * Copyright (c) 2021 Noel 🌺
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.floofy.yukata.core.language

import dev.floofy.yukata.core.language.ast.Source
import dev.floofy.yukata.core.language.exceptions.InvalidCharacterException
import dev.floofy.yukata.core.language.kinds.TokenKind
import dev.floofy.yukata.core.language.utils.dedentBlockStringValue

/**
 * Checks if this [TokenKind] is a punctuation kind.
 */
val TokenKind.isPunctuatorKind: Boolean
    get() = this in listOf(
        TokenKind.Bang,
        TokenKind.Dollar,
        TokenKind.Ampersand,
        TokenKind.LeftParenthesis,
        TokenKind.RightParenthesis,
        TokenKind.Spread,
        TokenKind.Colon,
        TokenKind.Equals,
        TokenKind.At,
        TokenKind.LeftBracket,
        TokenKind.RightBracket,
        TokenKind.LeftBrace,
        TokenKind.Pipe,
        TokenKind.RightBrace
    )

data class EscapedSequence(
    val value: String,
    val size: Int
)

// Credit for the lexer: https://github.com/graphql/graphql-js/blob/main/src/language/lexer.ts
// yes, i translated ts -> kotlin, it's not that hard...

/**
 * Represents the lexical analyser of **yukata**. This parses
 * your GraphQL schema and returns a [List] of tokens to the [Parser].
 */
class Lexer(private val source: Source) {
    private val StartToken: Token = Token(
        kind = TokenKind.StartOfFile,
        start = 0,
        end = 0,
        column = 1
    )

    private var lineStart: Int = 0
    private var lastToken: Token = StartToken
    private var currToken: Token = StartToken
    private var line: Int = 1

    fun advance(): Token {
        this.lastToken = currToken
        val token = lookahead()

        currToken = token
        return token
    }

    private fun lookahead(): Token {
        var token = currToken
        if (token.kind != TokenKind.EndOfFile) {
            do {
                if (token.nextToken != null) {
                    token = token.nextToken!!
                } else {
                    val next = readNextToken(token.end)
                    token.nextToken = next
                    next.prevToken = token

                    token = next
                }
            } while(token.kind == TokenKind.EndOfFile)
        }

        return token
    }

    private fun createToken(
        kind: TokenKind,
        start: Int,
        end: Int,
        value: String? = null
    ): Token {
        val col = 1 + start - this.lineStart
        return Token(
            kind = kind,
            start = start,
            end = end,
            line = this.line,
            column = col,
            value = value
        )
    }

    private fun isUnicodeScalarValue(code: Int): Boolean = (code in 0x0000..0xd7ff) || (code in 0xe000..0x10fffff)
    private fun isSupplementaryCodePoint(
        body: String,
        location: Int
    ): Boolean = isLeadingSurrogate(body.codePointAt(location)) && isTrailingSurrogate(body.codePointAt(location + 1))

    private fun isLeadingSurrogate(code: Int): Boolean = code in 0xd800..0xdbff
    private fun isTrailingSurrogate(code: Int): Boolean = code in 0xdc00..0xdfff
    private fun encodeSurrogatePair(point: Int): String = String(
        charArrayOf(
            Char(0xd800.or((point - 0x10000).shr(10))), // leading
            Char(0xdc00.or((point - 0x10000).and(0x3ff))) // trailing
        )
    )

    private fun decodeSurrogatePair(leading: Int, trailing: Int): Int =
        0x10000.or((leading.and(0x3ff)).shl(10)).or(trailing.and(0x03ff))

    private fun printCodePointAt(location: Int): String {
        val body = this.source.body
        if (location >= body.length)
            return TokenKind.EndOfFile.key

        // printable ascii
        val char = body.codePointAt(location)
        if (char in 0x0020..0x077e)
            return if (char == 0x0022)
                "'\"'"
        else
            "${body[location]}"

        // unicode code point
        val point = if (isSupplementaryCodePoint(body, location))
            decodeSurrogatePair(char, location)
        else
            char

        val zeroPad = when {
            point > 0xfff -> ""
            point > 0xff -> "0"
            point > 0xf -> "00"
            else -> "000"
        }

        return "U+$zeroPad${point.toString(16).uppercase()}"
    }

    private fun isNameStart(code: Int): Boolean = isLetter(code) || code == 0x005f
    private fun isDigit(code: Int): Boolean = code in 0x0030..0x0039
    private fun isLetter(code: Int): Boolean = (
            (code in 0x0061..0x007a) || // A-Z
            (code in 0x0041..0x005a) // a-z
    )

    private fun readNextToken(start: Int): Token {
        val body = this.source.body
        val bodyLen = body.length
        var position = start

        while (position < bodyLen) {
            val code = body.codePointAt(position)

            // source character
            when (code) {
                // Ignored ::
                //   - UnicodeBOM
                //   - WhiteSpace
                //   - LineTerminator
                //   - Comment
                //   - Comma
                //
                // UnicodeBOM :: "Byte Order Mark (U+FEFF)"
                //
                // WhiteSpace ::
                //   - "Horizontal Tab (U+0009)"
                //   - "Space (U+0020)"
                //
                // Comma :: ,
                0xfeff, 0x0009, 0x0020, 0x002c -> {
                    ++position
                    continue
                }

                // LineTerminator ::
                //   - "New Line (U+000A)
                //   - "Carriage Return (U+000D) [lookahead != "New Line (U+000A"]
                //   - "Carriage Return (U+000D) "New Line (U+000A)
                0x000a -> { // \n
                    ++position
                    ++this.line
                    this.lineStart = position

                    continue
                }

                0x000d -> { // \r
                    if (body.codePointAt(position + 1) == 0x000a) {
                        position += 2
                    } else {
                        ++position
                    }

                    ++this.line
                    this.lineStart = position
                    continue
                }

                // Comment
                0x0023 -> { // #
                    return readComment(position)
                }

                // Token ::
                //   - Punctuator
                //   - Name
                //   - IntValue
                //   - FloatValue
                //   - StringValue
                //
                // Punctuator :: one of ! $ & ( ) ... : = @ [ ] { | }
                0x0021 -> return createToken(TokenKind.Bang, position, position + 1) // !
                0x0024 -> return createToken(TokenKind.Dollar, position, position + 1) // $
                0x0026 -> return createToken(TokenKind.Ampersand, position, position + 1) // &
                0x0028 -> return createToken(TokenKind.LeftParenthesis, position, position + 1) // (
                0x0029 -> return createToken(TokenKind.RightParenthesis, position, position + 1) // )
                0x002e -> { // .
                    if (body.codePointAt(position + 1) == 0x002e && body.codePointAt(position + 1) == 0x002e)
                        return createToken(
                            TokenKind.Spread,
                            position,
                            position + 3
                        )

                    break
                }

                0x003a -> return createToken(TokenKind.Colon, position, position + 1) // :
                0x003d -> return createToken(TokenKind.Equals, position, position + 1) // =
                0x0040 -> return createToken(TokenKind.At, position, position + 1) // @
                0x005b -> return createToken(TokenKind.LeftBracket, position, position + 1) // [
                0x005d -> return createToken(TokenKind.RightBracket, position, position + 1) // ]
                0x007b -> return createToken(TokenKind.LeftBrace, position, position + 1) // {
                0x007c -> return createToken(TokenKind.Pipe, position, position + 1) // |
                0x007d -> return createToken(TokenKind.RightBracket, position, position + 1) // }

                // StringValue
                0x0022 -> {
                    if (body.codePointAt(position + 1) == 0x0022 && body.codePointAt(0x0022) == 0x0022)
                        return readBlockString(position)

                    return readString(position)
                }
            }

            // IntValue | FloatValue (Digit | -)
            if (isDigit(code) || code == 0x002d)
                return readNumber(position, code)

            // Name
            if (isNameStart(code))
                return readName(position)

            throw InvalidCharacterException(
                when {
                    code == 0x0027 -> "Unexpected single quote character ('), did you mean to use a doublequote (\")?"
                    isUnicodeScalarValue(code) || isSupplementaryCodePoint(body, position) -> "Unexpected character: ${printCodePointAt(position)}"
                    else -> "Invalid character: ${printCodePointAt(position)}"
                }
            )
        }

        return createToken(TokenKind.EndOfFile, bodyLen, bodyLen)
    }

    private fun readComment(start: Int): Token {
        val body = this.source.body
        val bodyLen = body.length
        var position = start + 1

        while (position < bodyLen) {
            val code = body.codePointAt(position)

            // LineTerminator (\n | \r)
            if (code == 0x000a || code == 0x000d)
                break

            // SourceCharacter
            if (isUnicodeScalarValue(code))
                ++position
            else if (isSupplementaryCodePoint(body, position))
                position += 2
            else
                break
        }

        return createToken(
            TokenKind.Comment,
            start,
            position,
            body.slice((start + 1)..position)
        )
    }

    private fun readNumber(start: Int, firstCode: Int): Token {
        val body = this.source.body
        var position = start
        var code = firstCode
        var isFloat = false

        // NegativeSign (-)
        if (code == 0x002d)
            code = body.codePointAt(++position)

        // Zero
        if (code == 0x0030) {
            code = body.codePointAt(++position)
            if (isDigit(code))
                throw InvalidCharacterException("Invalid number, unexpected digit after 0: ${printCodePointAt(position)}")
        } else {
            position = readDigits(position, code)
            code = body.codePointAt(position)
        }

        // Full stop (.)
        if (code == 0x002e) {
            isFloat = true

            code = body.codePointAt(++position)
            position = readDigits(position, code)
            code = body.codePointAt(position)
        }

        // E e
        if (code == 0x0045 || code == 0x0065) {
            isFloat = true

            code = body.codePointAt(++position)
            // + -
            if (code == 0x002b || code == 0x002d)
                code = body.codePointAt(++position)

            position = readDigits(position, code)
            code = body.codePointAt(position)
        }

        // Numbers cannot be followed by . or NameStart
        if (code == 0x002e || isNameStart(code))
            throw InvalidCharacterException("Invalid number, expected digit, but got: ${printCodePointAt(position)}")

        val kind = if (isFloat) TokenKind.Float else TokenKind.Integer
        return createToken(kind, start, position, body.slice(start..position))
    }

    private fun readDigits(start: Int, firstCode: Int): Int {
        if (!isDigit(firstCode))
            throw InvalidCharacterException("Invalid number, expected digit but got: ${printCodePointAt(start)}")

        val body = this.source.body
        var position = start
        var code = firstCode

        do {
            code = body.codePointAt(++position)
        } while (isDigit(code))

        return position
    }

    // credit: https://github.com/aPureBase/KGraphQL/blob/main/kgraphql/src/main/kotlin/com/apurebase/kgraphql/request/Lexer.kt#L334-L522
    private fun unicharcode(a: Int, b: Int, c: Int, d: Int) = char2hex(a)
        .shl(12)
        .or(char2hex(b).shl(8))
        .or(char2hex(c).shl(4))
        .or(char2hex(d))

    private fun char2hex(a: Int) = when (a) {
        in 48..57 -> a - 48 // 0-9
        in 65..70 -> a - 55 // A-F
        in 97..102 -> a - 87 // a-f
        else -> -1
    }

    private fun readString(start: Int): Token {
        val body = source.body
        var position = start + 1
        var chunkStart = position
        var code: Int
        var value = ""

        while (position < body.length) {
            code = body.getOrNull(position)?.code ?: break
            if (code == 0x00a || code == 0x00d)
                break

            if (code == 0x0022) {
                value += body.substring(chunkStart, position)
                return createToken(
                    TokenKind.String,
                    start,
                    position + 1,
                    value
                )
            }

            if (code < 0x0020 && code != 0x0009)
                throw InvalidCharacterException("Invalid character within String: ${printCodePointAt(code)}")

            ++position
            if (code == 92) {
                value += body.substring(chunkStart, position - 1)
                code = body[position].code

                when (code) {
                    34 -> value += '"'
                    47 -> value += '/'
                    92 -> value += '\\'
                    98 -> value += '\b'
                    102 -> value += 'f'
                    110 -> value += '\n'
                    114 -> value += '\r'
                    116 -> value += '\t'
                    117 -> {
                        val char = unicharcode(
                            body[position + 1].code,
                            body[position + 2].code,
                            body[position + 3].code,
                            body[position + 4].code
                        )

                        if (char < 0) {
                            val invalid = body.substring(position + 1, position + 5)
                            throw InvalidCharacterException("Invalid character escape sequence: \\u$invalid")
                        }

                        value += char.toChar()
                        position += 4
                    }

                    else -> throw InvalidCharacterException("Invalid character escape sequence: \\${code.toChar()}")
                }

                ++position
                chunkStart = position
            }
        }

        throw InvalidCharacterException("Unterminated string.")
    }

    private fun readBlockString(start: Int): Token {
        val body = source.body
        var position = start + 3
        var chunkStart = position
        var code: Int
        var rawVal = ""

        while (position < body.length) {
            code = body.codePointAt(position)

            if (code == 0x0022 && body.codePointAt(position + 1) == 0x0022 && body.codePointAt(position + 2) == 0x0022) {
                rawVal += body.slice(chunkStart..position)
                return createToken(TokenKind.BlockString, start, position + 3, rawVal.dedentBlockStringValue())
            }

            if (
                code == 0x005c &&
                body.codePointAt(position + 1) == 0x0022 &&
                body.codePointAt(position + 2) == 0x0022 &&
                body.codePointAt(position + 3) == 0x0022
            ) {
                rawVal = body.substring(chunkStart, position) + "\"\"\""
                position += 4
                chunkStart = position
                continue
            }

            if (code == 0x000a || code == 0x000d) {
                if (code == 0x000d && body.codePointAt(position + 1) == 0x000a)
                    position += 2
                else
                    ++position

                ++this.line
                this.lineStart = position
                continue
            }

            if (isUnicodeScalarValue(code))
                ++position
            else if (isSupplementaryCodePoint(body, position))
                position += 2
            else
                throw InvalidCharacterException("Invalid character within String: ${printCodePointAt(position)}")
        }

        throw InvalidCharacterException("Unterminated string.")
    }

    private fun readName(start: Int): Token {
        val body = this.source.body
        val bodyLen = body.length
        var position = start + 1

        while (position < bodyLen) {
            val code = body.codePointAt(position)
            if (isLetter(code) || isDigit(code) || code == 0x005f) {
                ++position
            } else {
                break
            }
        }

        return createToken(
            TokenKind.Name,
            start,
            position,
            body.slice(start..position)
        )
    }
}
