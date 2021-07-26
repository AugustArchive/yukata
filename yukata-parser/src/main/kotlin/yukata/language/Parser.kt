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

package yukata.language

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import yukata.language.ast.ASTNode
import yukata.language.exceptions.GraphQLException
import yukata.language.nodes.*

/**
 * Parses the [source] object with any additional [options][Parser.Options] and returns
 * a [DocumentNode].
 *
 * @param source The source object
 * @param block Any additional options
 * @return A [DocumentNode]. :3
 */
@OptIn(ExperimentalContracts::class)
fun parse(source: Source, block: Parser.Options.() -> Unit): DocumentNode {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val parser = Parser(source, Parser.Options().apply(block))
    return parser.parseDocument()
}

/**
 * This is step 2 of the pipeline after the [lexical analysis][Lexer]: the [Parser]!~
 * The parser generates [nodes][yukata.language.ast.ASTNode] from the [Token]s that the [lexer][Lexer] has analyzed.
 *
 * @param source The source object for this [Parser].
 * @param options Any additional options to extend this [Parser].
 */
class Parser(
    private val source: Source,
    private val options: Options = Options.Default
) {
    /**
     * Represents the options to customize this [Parser] object.
     */
    data class Options(
        /**
         * If all AST nodes should include a [location][yukata.language.ast.ASTNode.location] property.
         */
        var noLocation: Boolean = false
    ) {
        companion object {
            val Default: Options = Options()
        }
    }

    private val lexer: Lexer = Lexer(source)

    private fun getTokenDesc(token: Token): String {
        val value = token.value
        return getTokenKindDesc(token.kind) + (if (value != null) " \"$value\"" else "")
    }

    private fun getTokenKindDesc(kind: TokenKind): String =
        if (kind.isPunctuatorKind) "\"$kind\"" else kind.key

    private fun <T: ASTNode> createNode(startToken: Token, node: T): T {
        if (!options.noLocation) {
            node.location = SourceLocation(
                startToken,
                lexer.lastToken,
                lexer.source
            )
        }

        return node
    }

    private fun peek(kind: TokenKind): Boolean = lexer.currToken.kind == kind

    private fun expectToken(kind: TokenKind): Token {
        val token = lexer.currToken
        if (token.kind == kind) {
            lexer.advance()
            return token
        }

        throw GraphQLException.asSyntaxError(
            "Expected ${getTokenKindDesc(kind)}, found ${getTokenDesc(token)}",
            lexer.source,
            token.start
        )
    }

    private fun expectOptionalToken(kind: TokenKind): Boolean {
        val token = lexer.currToken
        if (token.kind == kind) {
            lexer.advance()
            return true
        }

        return false
    }

    private fun expectKeyword(value: String) {
        val token = lexer.currToken
        if (
            token.kind == TokenKind.Name &&
            token.value == value
        ) {
            lexer.advance()
        } else {
            throw GraphQLException.asSyntaxError(
                "Expected \"$value\", found ${getTokenDesc(token)}",
                lexer.source,
                token.start
            )
        }
    }

    private fun expectOptionalKeyword(value: String): Boolean {
        val token = lexer.currToken
        if (token.kind == TokenKind.Name && token.value == value) {
            lexer.advance()
            return true
        }

        return false
    }

    private fun unexpected(token: Token? = null): GraphQLException {
        val t = token ?: lexer.currToken
        return GraphQLException.asSyntaxError(
            "Unexpected ${getTokenDesc(t)}",
            lexer.source,
            t.start
        )
    }

    private inline fun <reified T> any(
        openKind: TokenKind,
        parseFn: () -> T,
        closeKind: TokenKind
    ): List<T> {
        expectToken(openKind)
        val nodes = mutableListOf<T>()
        while (!expectOptionalToken(closeKind))
            nodes.add(parseFn())

        return nodes.toList()
    }

    private inline fun <reified T> optionalMany(
        openKind: TokenKind,
        parseFn: () -> T,
        closeKind: TokenKind
    ): List<T> {
        if (expectOptionalToken(openKind)) {
            val nodes = mutableListOf<T>()
            do {
                nodes.add(parseFn())
            } while (!expectOptionalToken(closeKind))

            return nodes.toList()
        }

        return emptyList()
    }

    private inline fun <reified T> many(
        openKind: TokenKind,
        parseFn: () -> T,
        closeKind: TokenKind
    ): List<T> {
        expectToken(openKind)
        val nodes = mutableListOf<T>()
        do {
            nodes.add(parseFn())
        } while (!expectOptionalToken(closeKind))

        return nodes.toList()
    }

    private inline fun <reified T> delimitedAny(
        kind: TokenKind,
        parseFn: () -> T
    ): List<T> {
        expectOptionalToken(kind)
        val nodes = mutableListOf<T>()

        do {
            nodes.add(parseFn())
        } while (expectOptionalToken(kind))

        return nodes
    }

    /**
     * Converts a name lex token to a [NameNode].
     */
    @Suppress("UNUSED")
    fun parseName(): NameNode {
        val token = expectToken(TokenKind.Name)
        return createNode(token, NameNode(value = token.value!!, location = null))
    }

    /**
     * Document : Definition+
     */
    @Suppress("UNUSED")
    fun parseDocument(): DocumentNode = createNode(lexer.currToken, DocumentNode(
        location = null,
        definitions = many(
            TokenKind.StartOfFile,
            ::parseDefinition,
            TokenKind.EndOfFile
        )
    ))

    /**
     * Definition :
     *    - ExecutableDefinition
     *    - TypeSystemDefinition
     *
     * ExecutableDefinition :
     *    - OperationDefinition
     *    - FragmentDefinition
     */
    @Suppress("UNUSED")
    fun parseDefinition(): DefinitionNode {
        if (peek(TokenKind.Name)) {
            when (lexer.currToken.value) {
                "query", "mutation" -> return parseOperationDefinition()
                "fragment" -> return parseFragmentDefinition()
                "schema", "scalar", "type", "interface", "union", "enum", "input", "directive" -> return parseTypeSystemDefinition()
                "extend", "subscription" -> throw GraphQLException.asSyntaxError(
                    "`extend` and `subscription` are not supported as of this version of yukata-parser",
                    lexer.source,
                    lexer.currToken.start
                )
            }
        } else if (peek(TokenKind.LeftBrace)) {
            return parseOperationDefinition()
        } else if (peekDescription()) {
            return parseTypeSystemDefinition()
        }

        throw unexpected()
    }

    /**
     * OperationDefinition :
     *    - SelectionSet
     *    - OperationType Name? VariableDefinitions? Directives? SelectionSet
     */
    @Suppress("UNUSED")
    fun parseOperationDefinition(): OperationDefinitionNode {
        val start = lexer.currToken
        if (peek(TokenKind.LeftBrace)) {
            return createNode(start, OperationDefinitionNode(
                location = null,
                operation = OperationKind.QUERY,
                name = null,
                variables = listOf(),
                directives = listOf(),
                selectionSet = parseSelectionSet()
            ))
        }

        val kind = parseOperationType()
        var name: NameNode? = null

        if (peek(TokenKind.Name))
            name = parseName()

        return createNode(start, OperationDefinitionNode(
            location = null,
            operation = kind,
            name = name,
            variables = parseVariableDefinitions(),
            directives = parseDirectives(false),
            selectionSet = parseSelectionSet()
        ))
    }

    /**
     * OperationType : one of query, mutation, or subscription
     */
    @Suppress("UNUSED")
    fun parseOperationType(): OperationKind {
        val token = expectToken(TokenKind.Name)
        when (token.value) {
            "query" -> return OperationKind.QUERY
            "mutation" -> return OperationKind.MUTATION
        }

        throw unexpected(token)
    }

    /**
     * VariableDefinitions : ( VariableDefinition+ )
     */
    @Suppress("UNUSED")
    fun parseVariableDefinitions(): List<VariableDefinitionNode> = optionalMany(
        TokenKind.LeftParenthesis,
        ::parseVariableDefinition,
        TokenKind.RightParenthesis
    )

    /**
     * VariableDefinition : Variable : Type DefaultValue? Directives[Const]?
     */
    @Suppress("UNUSED")
    fun parseVariableDefinition(): VariableDefinitionNode = createNode(
        lexer.currToken,
        VariableDefinitionNode(
            variable = parseVariable(),
            type = expectToken(TokenKind.Colon).let { parseTypeReference() },
            defaultValue = if (expectOptionalToken(TokenKind.Equals)) parseConstValueLiteral() else null,
            directives = parseConstDirectives(),
            location = null
        )
    )

    /**
     * Variable : $ Name
     */
    @Suppress("UNUSED")
    fun parseVariable(): VariableValueNode {
        val start = lexer.currToken
        expectToken(TokenKind.Dollar)

        return createNode(start, VariableValueNode(
            name = parseName(),
            location = null
        ))
    }

    /**
     * SelectionSet : { Selection+ }
     */
    @Suppress("UNUSED")
    fun parseSelectionSet(): SelectionSetNode = createNode(
        lexer.currToken,
        SelectionSetNode(
            selectors = many(
                TokenKind.LeftBrace,
                ::parseSelection,
                TokenKind.RightBrace
            ),

            location = null
        )
    )

    /**
     * Selection :
     *    - Field
     *    - FragmentSpread
     *    - InlineFragment
     */
    @Suppress("UNUSED")
    fun parseSelection(): SelectionNode = if (peek(TokenKind.Spread))
        parseFragment()
    else
        parseField()

    /**
     * Field : Alias? Name Arguments? Directives? SelectionSet?
     *
     * Alias : Name :
     */
    @Suppress("UNUSED")
    fun parseField(): FieldSelectionNode {
        val start = lexer.currToken
        val nameOrAlias = parseName()

        var alias: NameNode? = null
        val name: NameNode

        if (expectOptionalToken(TokenKind.Colon)) {
            alias = nameOrAlias
            name = parseName()
        } else {
            name = nameOrAlias
        }

        return createNode(start, FieldSelectionNode(
            location = null,
            alias = alias,
            name = name,
            arguments = parseArguments(false),
            directives = parseDirectives(false),
            selectionSet = if (peek(TokenKind.LeftBrace)) parseSelectionSet() else null
        ))
    }

    /**
     * Arguments[Const] : ( Argument[?Const]+ )
     */
    @Suppress("UNUSED")
    fun parseArguments(isConst: Boolean = false): List<ArgumentNode> {
        val invocation = { parseArgument(isConst) }
        return optionalMany(
            TokenKind.LeftParenthesis,
            invocation,
            TokenKind.RightParenthesis
        )
    }

    /**
     * Argument[Const] : Name : Value[?Const]
     */
    @Suppress("UNUSED")
    fun parseArgument(isConst: Boolean = false): ArgumentNode {
        val start = lexer.currToken
        val name = parseName()

        expectToken(TokenKind.Colon)
        return createNode(start, ArgumentNode(
            name = name,
            value = parseValueLiteral(isConst),
            location = null
        ))
    }

    @Suppress("UNUSED")
    fun parseConstArgument(): ConstArgumentNode = parseArgument(true) as ConstArgumentNode

    /**
     * Fragment Spread : ... FragmentName Directives?
     *
     * InlineFragment : ... TypeCondition? Directives? SelectionSet
     */
    @Suppress("UNUSED")
    fun parseFragment(): SelectionNode {
        val start = lexer.currToken
        expectToken(TokenKind.Spread)

        val hasTypeCondition = expectOptionalKeyword("on")
        if (!hasTypeCondition && peek(TokenKind.Name))
            return createNode(start, FragmentSpreadSelectionNode(
                name = parseFragmentName(),
                directives = parseDirectives(false),
                location = null
            ))

        return createNode(start, InlineFragmentSelectionNode(
            location = null,
            typeCondition = if (hasTypeCondition) parseNamedType() else null,
            directives = parseDirectives(false),
            selectionSet = parseSelectionSet()
        ))
    }

    /**
     * FragmentDefinition :
     *    - fragment FragmentName on TypeCondition Directives? SelectionSet
     *
     * TypeCondition : NamedType
     */
    @Suppress("UNUSED")
    fun parseFragmentDefinition(): FragmentDefinitionNode {
        val start = lexer.currToken
        expectKeyword("fragment")

        return createNode(start, FragmentDefinitionNode(
            location = null,
            name = parseFragmentName(),
            condition = expectKeyword("on").let { parseNamedType() },
            directives = parseDirectives(false),
            selectionSet = parseSelectionSet()
        ))
    }

    /**
     * FragmentName : Name but not `on`
     */
    @Suppress("UNUSED")
    fun parseFragmentName(): NameNode {
        if (lexer.currToken.value == "on")
            throw unexpected()

        return parseName()
    }

    /**
     * Value[Const] :
     *    - [~Const] Variable
     *    - IntValue
     *    - FloatValue
     *    - StringValue
     *    - BooleanValue
     *    - NullValue
     *    - EnumValue
     *    - ListValue[?Const]
     *    - ObjectValue[?Const]
     *
     * BooleanValue : one of `true` `false`
     *
     * NullValue : `null`
     *
     * EnumValue : Name but not `true`, `false`, or `null`
     */
    @Suppress("UNUSED")
    fun parseValueLiteral(isConst: Boolean = false): ValueNode {
        val token = lexer.currToken
        when (token.kind) {
            TokenKind.LeftBracket -> return parseList(isConst)
            TokenKind.LeftBrace -> return parseObject(isConst)
            TokenKind.Integer -> {
                lexer.advance()
                return createNode(token, IntValueNode(
                    location = null,
                    value = token.value!!.toInt()
                ))
            }

            TokenKind.Float -> {
                lexer.advance()
                return createNode(token, DoubleValueNode(
                    location = null,
                    value = token.value!!.toDouble()
                ))
            }

            TokenKind.String, TokenKind.BlockString -> return parseStringLiteral()
            TokenKind.Name -> {
                lexer.advance()
                when (token.value) {
                    "true" -> return createNode(token, BooleanValueNode(
                        location = null,
                        value = true
                    ))

                    "false" -> return createNode(token, BooleanValueNode(
                        location = null,
                        value = false
                    ))

                    "null" -> return createNode(token, NullValueNode(location = null))

                    else -> return createNode(token, EnumValueNode(
                        location = null,
                        value = token.value!!
                    ))
                }
            }

            TokenKind.Dollar -> {
                if (isConst) {
                    expectToken(TokenKind.Dollar)
                    if (lexer.currToken.kind == TokenKind.Name) {
                        val name = lexer.currToken.value
                        throw GraphQLException.asSyntaxError(
                            "Unexpected variable \"$$name\" in constant value.",
                            lexer.source,
                            token.start
                        )
                    }

                    throw unexpected(token)
                }
            }

            else -> return parseVariable()
        }

        throw unexpected()
    }

    @Suppress("UNUSED")
    fun parseConstValueLiteral(): ConstValueNode = parseValueLiteral(true) as ConstValueNode

    @Suppress("UNUSED")
    fun parseStringLiteral(): StringValueNode {
        val token = lexer.currToken
        lexer.advance()

        return createNode(token, StringValueNode(
            location = null,
            value = token.value!!,
            isBlock = token.kind == TokenKind.BlockString
        ))
    }

    /**
     * ListValue[Const] :
     *    - [ ]
     *    - [ Value[?Const]+ ]
     */
    @Suppress("UNUSED")
    fun parseList(isConst: Boolean = false): ArrayValueNode {
        val owo = { parseValueLiteral(isConst) }
        return createNode(lexer.currToken, ArrayValueNode(
            location = null,
            values = any(
                TokenKind.LeftBracket,
                owo,
                TokenKind.RightBracket
            )
        ))
    }

    /**
     * ObjectValue[Const] :
     *    - { }
     *    - { ObjectField[?Const]+ }
     */
    @Suppress("UNUSED")
    fun parseObject(isConst: Boolean): ObjectValueNode {
        val start = lexer.currToken
        val owo = { parseObjectField(isConst) }

        return createNode(start, ObjectValueNode(
            location = null,
            members = any(
                TokenKind.LeftBrace,
                owo,
                TokenKind.RightBrace
            )
        ))
    }

    /**
     * ObjectField[Const] : Name : Value[?Const]
     */
    @Suppress("UNUSED")
    fun parseObjectField(isConst: Boolean): ObjectValueNode.ObjectMemberValueNode {
        val start = lexer.currToken
        val name = parseName()

        expectToken(TokenKind.Colon)
        return createNode(start, ObjectValueNode.ObjectMemberValueNode(
            location = null,
            name = name,
            value = parseValueLiteral(isConst)
        ))
    }

    @Suppress("UNUSED")
    fun parseDirectives(isConst: Boolean = false): List<DirectiveNode> {
        val directives = mutableListOf<DirectiveNode>()
        while (peek(TokenKind.At))
            directives.add(parseDirective(isConst))

        return directives.toList()
    }

    @Suppress("UNCHECKED_CAST", "UNUSED")
    fun parseConstDirectives(): List<ConstDirectiveNode> = parseDirectives(true) as List<ConstDirectiveNode>

    @Suppress("UNUSED")
    fun parseDirective(isConst: Boolean = false): DirectiveNode {
        val start = lexer.currToken
        expectToken(TokenKind.At)

        return createNode(start, DirectiveNode(
            location = null,
            name = parseName(),
            args = parseArguments(isConst)
        ))
    }

    /**
     * Type :
     *    - NamedType
     *    - ListType
     *    - NonNullType
     */
    @Suppress("UNUSED")
    fun parseTypeReference(): TypeNode {
        val start = lexer.currToken
        var type: TypeNode

        if (expectOptionalToken(TokenKind.LeftBracket)) {
            val inner = parseTypeReference()
            expectToken(TokenKind.RightBracket)

            type = createNode(start, ListTypeNode(
                location = null,
                type = inner
            ))
        } else {
            type = parseNamedType()
        }

        if (expectOptionalToken(TokenKind.Bang)) {
            return createNode(start, NonNulledTypeNode(
                location = null,
                type = type
            ))
        }

        return type
    }

    /**
     * NamedType : Name
     */
    @Suppress("UNUSED")
    fun parseNamedType(): NamedTypeNode = createNode(lexer.currToken, NamedTypeNode(
        location = null,
        name = parseName()
    ))

    @Suppress("UNUSED")
    fun peekDescription(): Boolean = peek(TokenKind.String) || peek(TokenKind.BlockString)

    /**
     * TypeSystemDefinition :
     *   - SchemaDefinition
     *   - TypeDefinition
     *   - DirectiveDefinition
     *
     * TypeDefinition :
     *   - ScalarTypeDefinition
     *   - ObjectTypeDefinition
     *   - InterfaceTypeDefinition
     *   - UnionTypeDefinition
     *   - EnumTypeDefinition
     *   - InputObjectTypeDefinition
     */
    @Suppress("UNUSED")
    fun parseTypeSystemDefinition(): TypeSystemDefinitionNode {
        val keyword = if (peekDescription()) lexer.lookahead() else lexer.currToken

        if (keyword.kind == TokenKind.Name) {
            when (keyword.value) {
                "schema" -> return parseSchemaDefinition()
                "scalar" -> return parseScalarDefinition()
                "type" -> return parseObjectTypeDefinition()
                "interface" -> return parseInterfaceTypeDefinition()
                "union" -> return parseUnionTypeDefinition()
                "enum" -> return parseEnumTypeDefinition()
                "input" -> return parseInputObjectTypeDefinition()
                "directive" -> return parseDirectiveDefinition()
            }
        }

        throw unexpected(keyword)
    }

    /**
     * Description : StringValue?
     */
    @Suppress("UNUSED")
    fun parseDescription(): StringValueNode? = if (peekDescription())
        parseStringLiteral()
    else
        null

    /**
     * SchemaDefinition : Description? schema Directives[Const]? { OperationTypeDefinition+ }
     */
    @Suppress("UNUSED")
    fun parseSchemaDefinition(): SchemaDefinitionNode {
        val start = lexer.currToken
        val description = parseDescription()
        expectKeyword("schema")

        val directives = parseConstDirectives()
        val operationTypes = many(
            TokenKind.LeftBrace,
            ::parseOperationTypeDefinition,
            TokenKind.LeftBrace
        )

        @Suppress("UNCHECKED_CAST")
        return createNode(start, SchemaDefinitionNode(
            location = null,
            description = description,
            directives = directives,
            operationTypes = operationTypes
        ))
    }

    /**
     * OperationTypeDefinition : OperationType : NamedType
     */
    @Suppress("UNUSED")
    fun parseOperationTypeDefinition(): OperationTypeDefinitionNode {
        val start = lexer.currToken
        val operation = parseOperationType()
        expectToken(TokenKind.Colon)

        val type = parseNamedType()
        return createNode(start, OperationTypeDefinitionNode(
            location = null,
            operation = operation,
            type = type
        ))
    }

    /**
     * ScalarTypeDefinition : Description? scalar Name Directives[Const]?
     */
    @Suppress("UNUSED")
    fun parseScalarDefinition(): ScalarTypeDefinitionNode {
        val start = lexer.currToken
        val description = parseDescription()
        expectKeyword("scalar")

        val name = parseName()
        val directives = parseConstDirectives()
        return createNode(start, ScalarTypeDefinitionNode(
            location = null,
            description, name, directives
        ))
    }

    /**
     * ObjectTypeDefinition :
     *   Description?
     *   type Name ImplementsInterfaces? Directives[Const]? FieldsDefinition?
     */
    @Suppress("UNUSED")
    fun parseObjectTypeDefinition(): ObjectTypeDefinitionNode {
        val start = lexer.currToken
        val description = parseDescription()
        expectKeyword("type")

        val name = parseName()
        val interfaces = parseImplementsInterfaces()
        val directives = parseConstDirectives()
        val fields = parseFieldsDefinition()

        return createNode(start, ObjectTypeDefinitionNode(
            location = null,
            description, name, directives, interfaces, fields
        ))
    }

    /**
     * ImplementsInterfaces :
     *   - implements `&`? NamedType
     *   - ImplementsInterfaces & NamedType
     */
    @Suppress("UNUSED")
    fun parseImplementsInterfaces(): List<NamedTypeNode> = if (expectOptionalKeyword("implements"))
        delimitedAny(TokenKind.Ampersand, ::parseNamedType)
    else
        listOf()

    /**
     * FieldsDefinition : { FieldDefinition+ }
     */
    @Suppress("UNUSED")
    fun parseFieldsDefinition(): List<FieldDefinitionNode> = optionalMany(
        TokenKind.LeftBrace,
        ::parseFieldDefinition,
        TokenKind.RightBrace
    )

    /**
     * FieldDefinition :
     *   - Description? Name ArgumentsDefinition? : Type Directives[Const]?
     */
    @Suppress("UNUSED")
    fun parseFieldDefinition(): FieldDefinitionNode {
        val start = lexer.currToken
        val description = parseDescription()
        val name = parseName()
        val arguments = parseArgumentDefinitions()
        expectToken(TokenKind.Colon)

        val type = parseTypeReference()
        val directives = parseConstDirectives()
        return createNode(start, FieldDefinitionNode(
            location = null,
            description, name, arguments, type, directives
        ))
    }

    /**
     * ArgumentsDefinition : ( InputValueDefinition+ )
     */
    @Suppress("UNUSED")
    fun parseArgumentDefinitions(): List<InputValueNode> = optionalMany(
        TokenKind.LeftParenthesis,
        ::parseInputValueDefinition,
        TokenKind.RightParenthesis
    )

    /**
     * InputValueDefinition :
     *   - Description? Name : Type DefaultValue? Directives[Const]?
     */
    @Suppress("UNUSED")
    fun parseInputValueDefinition(): InputValueNode {
        val start = lexer.currToken
        val description = parseDescription()
        val name = parseName()
        expectToken(TokenKind.Colon)

        val type = parseTypeReference()
        var defaultValue: ConstValueNode? = null

        if (expectOptionalToken(TokenKind.Equals))
            defaultValue = parseConstValueLiteral()

        val directives = parseConstDirectives()
        return createNode(start, InputValueNode(
            location = null,
            description = description,
            name = name,
            type = type,
            defaultValue = defaultValue,
            directives = directives
        ))
    }

    /**
     * InterfaceTypeDefinition :
     *   - Description? interface Name Directives[Const]? FieldsDefinition?
     */
    @Suppress("UNUSED")
    fun parseInterfaceTypeDefinition(): InterfaceTypeDefinitionNode {
        val start = lexer.currToken
        val description = parseDescription()
        expectKeyword("interface")

        val name = parseName()
        val interfaces = parseImplementsInterfaces()
        val directives = parseConstDirectives()
        val fields = parseFieldsDefinition()

        return createNode(start, InterfaceTypeDefinitionNode(
            location = null,
            description, name, directives, interfaces, fields
        ))
    }

    /**
     * UnionTypeDefinition :
     *   - Definition? union Name Directives[Const]? UnionMemberTypes?
     */
    @Suppress("UNUSED")
    fun parseUnionTypeDefinition(): UnionTypeDefinitionNode {
        val start = lexer.currToken
        val description = parseDescription()
        expectKeyword("union")

        val name = parseName()
        val directives = parseConstDirectives()
        val types = parseUnionMemberTypes()
        return createNode(start, UnionTypeDefinitionNode(
            location = null,
            description, name, directives, types
        ))
    }

    /**
     * UnionMemberTypes :
     *   - = `|`? NamedType
     *   - UnionMemberTypes | NamedType
     */
    @Suppress("UNUSED")
    fun parseUnionMemberTypes(): List<NamedTypeNode> = if (expectOptionalToken(TokenKind.Equals))
        delimitedAny(TokenKind.Pipe, ::parseNamedType)
    else
        listOf()

    /**
     * EnumTypeDefinition :
     *    - Description? enum Name Directives[Const]? EnumValuesDefinition?
     */
    @Suppress("UNUSED")
    fun parseEnumTypeDefinition(): EnumTypeDefinitionNode {
        val start = lexer.currToken
        val description = parseDescription()
        expectKeyword("enum")

        val name = parseName()
        val directives = parseConstDirectives()
        val values = parseEnumValuesDefinition()
        return createNode(start, EnumTypeDefinitionNode(
            location = null,
            description = description,
            name = name,
            directives = directives,
            values = values
        ))
    }

    /**
     * EnumValuesDefinition : { EnumValueDefinition+ }
     */
    @Suppress("UNUSED")
    fun parseEnumValuesDefinition(): List<EnumValueDefinitionNode> = optionalMany(
        TokenKind.LeftBrace,
        ::parseEnumValueDefinition,
        TokenKind.RightBrace
    )

    /**
     * EnumValueDefinition : Description? EnumValue Directives[Const]?
     *
     * EnumValue : Name
     */
    @Suppress("UNUSED")
    fun parseEnumValueDefinition(): EnumValueDefinitionNode {
        val start = lexer.currToken
        val description = parseDescription()
        val name = parseName()
        val directives = parseConstDirectives()

        return createNode(start, EnumValueDefinitionNode(
            location = null,
            description = description,
            name = name,
            directives = directives
        ))
    }

    /**
     * InputObjectTypeDefinition :
     *   - Description? input Name Directives[Const]? InputFieldsDefinition?
     */
    @Suppress("UNUSED")
    fun parseInputObjectTypeDefinition(): InputObjectTypeDefinitionNode {
        val start = lexer.currToken
        val description = parseDescription()
        expectKeyword("input")

        val name = parseName()
        val directives = parseConstDirectives()
        val fields = parseInputFieldsDefinition()
        return createNode(start, InputObjectTypeDefinitionNode(
            location = null,
            description = description,
            name = name,
            directives = directives,
            fields = fields
        ))
    }

    /**
     * InputFieldsDefinition : { InputValueDefinition+ }
     */
    @Suppress("UNUSED")
    fun parseInputFieldsDefinition(): List<InputValueNode> = optionalMany(
        TokenKind.LeftBrace,
        ::parseInputValueDefinition,
        TokenKind.RightBrace
    )

    /**
     * DirectiveDefinition :
     *    - Description? directive @ Name ArgumentsDefinition? `repeatable`? on DirectiveLocations
     */
    @Suppress("UNUSED")
    fun parseDirectiveDefinition(): DirectiveTypeDefinitionNode {
        val start = lexer.currToken
        val description = parseDescription()
        expectKeyword("directive")
        expectToken(TokenKind.At)

        val name = parseName()
        val args = parseArgumentDefinitions()
        val repeatable = expectOptionalKeyword("repeatable")
        expectKeyword("on")

        val locations = parseDirectiveLocations()
        return createNode(start, DirectiveTypeDefinitionNode(
            location = null,
            description = description,
            name = name,
            arguments = args,
            repeatable = repeatable,
            locations = locations
        ))
    }

    /**
     * DirectiveLocations :
     *    - `|`? DirectiveLocation
     *    - DirectiveLocations | DirectiveLocation
     */
    @Suppress("UNUSED")
    fun parseDirectiveLocations(): List<NameNode> = delimitedAny(
        TokenKind.Pipe,
        ::parseDirectiveLocation
    )

    /**
     * DirectiveLocation :
     *   - ExecutableDirectiveLocation
     *   - TypeSystemDirectiveLocation
     *
     * ExecutableDirectiveLocation : one of
     *   `QUERY`
     *   `MUTATION`
     *   `SUBSCRIPTION`
     *   `FIELD`
     *   `FRAGMENT_DEFINITION`
     *   `FRAGMENT_SPREAD`
     *   `INLINE_FRAGMENT`
     *
     * TypeSystemDirectiveLocation : one of
     *   `SCHEMA`
     *   `SCALAR`
     *   `OBJECT`
     *   `FIELD_DEFINITION`
     *   `ARGUMENT_DEFINITION`
     *   `INTERFACE`
     *   `UNION`
     *   `ENUM`
     *   `ENUM_VALUE`
     *   `INPUT_OBJECT`
     *   `INPUT_FIELD_DEFINITION`
     */
    @Suppress("UNUSED")
    fun parseDirectiveLocation(): NameNode {
        val start = lexer.currToken
        val name = parseName()
        if (DirectiveLocation.asString(name.value) != null)
            return name

        throw unexpected(start)
    }
}
