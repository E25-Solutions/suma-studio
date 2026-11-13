package com.e25solutions.sumastudio.math

import com.ionspin.kotlin.bignum.decimal.BigDecimal

internal sealed class Token {
    data class Number(val value: BigDecimal) : Token()
    data class Operator(val symbol: Char) : Token()
    data object LeftParen : Token()
    data object RightParen : Token()
}

internal class Tokenizer(private val input: String) {

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < input.length && (input[i].isDigit() || input[i] == '.')) i++
                    val text = input.substring(start, i)
                    if (text.count { it == '.' } > 1 || text == ".") {
                        throw MathError.Syntax("número inválido \"$text\"")
                    }
                    tokens += Token.Number(BigDecimal.parseString(normalize(text)))
                }
                c in "+-*/×÷−" -> {
                    tokens += Token.Operator(canonical(c))
                    i++
                }
                c == '(' -> { tokens += Token.LeftParen; i++ }
                c == ')' -> { tokens += Token.RightParen; i++ }
                else -> throw MathError.Syntax("carácter inesperado '$c'")
            }
        }
        return tokens
    }

    private fun normalize(text: String): String = when {
        text.startsWith(".") -> "0$text"
        text.endsWith(".") -> text.dropLast(1)
        else -> text
    }

    private fun canonical(c: Char): Char = when (c) {
        '×' -> '*'
        '÷' -> '/'
        '−' -> '-'
        else -> c
    }
}

/**
 * Analizador descendente recursivo:
 *
 *   expression := term (('+' | '-') term)*
 *   term       := unary (('*' | '/') unary)*
 *   unary      := ('+' | '-') unary | primary
 *   primary    := number | '(' expression ')'
 */
internal class Parser(private val tokens: List<Token>) {
    private var position = 0

    fun parse(): BigDecimal {
        if (tokens.isEmpty()) throw MathError.Syntax("expresión vacía")
        val result = expression()
        if (position < tokens.size) throw MathError.Syntax("símbolo sobrante")
        return result
    }

    private fun expression(): BigDecimal {
        var value = term()
        while (true) {
            value = when {
                match('+') -> value + term()
                match('-') -> value - term()
                else -> return value
            }
        }
    }

    private fun term(): BigDecimal {
        var value = unary()
        while (true) {
            value = when {
                match('*') -> value * unary()
                match('/') -> Evaluator.divide(value, unary())
                else -> return value
            }
        }
    }

    private fun unary(): BigDecimal = when {
        match('-') -> unary().negate()
        match('+') -> unary()
        else -> primary()
    }

    private fun primary(): BigDecimal {
        return when (val token = tokens.getOrNull(position++)) {
            is Token.Number -> token.value
            Token.LeftParen -> {
                val value = expression()
                if (tokens.getOrNull(position++) != Token.RightParen) {
                    throw MathError.Syntax("falta cerrar paréntesis")
                }
                value
            }
            null -> throw MathError.Syntax("expresión incompleta")
            else -> throw MathError.Syntax("símbolo inesperado")
        }
    }

    private fun match(symbol: Char): Boolean {
        val token = tokens.getOrNull(position)
        if (token is Token.Operator && token.symbol == symbol) {
            position++
            return true
        }
        return false
    }
}
