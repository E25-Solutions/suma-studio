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
                c in "+-*/×÷−%" -> {
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
 *   term       := operand (('*' | '/') operand)*
 *   operand    := unary '%'?
 *   unary      := ('+' | '-') unary | primary
 *   primary    := number | '(' expression ')'
 *
 * Porcentaje, como en las calculadoras de bolsillo:
 *   a + b%  →  a + a·b/100      (50 + 10% = 55)
 *   a − b%  →  a − a·b/100      (50 − 10% = 45)
 *   en cualquier otro lugar, b% = b/100   (50 × 10% = 5)
 */
internal class Parser(private val tokens: List<Token>) {
    private var position = 0

    /** Un valor que recuerda si se escribió como porcentaje suelto (sin × ni ÷). */
    private class Operand(val value: BigDecimal, val isPercent: Boolean) {
        val resolved: BigDecimal get() = if (isPercent) Evaluator.divide(value, HUNDRED) else value
    }

    fun parse(): BigDecimal {
        if (tokens.isEmpty()) throw MathError.Syntax("expresión vacía")
        val result = expression()
        if (position < tokens.size) throw MathError.Syntax("símbolo sobrante")
        return result
    }

    private fun expression(): BigDecimal {
        var value = term().resolved
        while (true) {
            value = when {
                match('+') -> value + additive(value, term())
                match('-') -> value - additive(value, term())
                else -> return value
            }
        }
    }

    private fun additive(base: BigDecimal, right: Operand): BigDecimal =
        if (right.isPercent) Evaluator.divide(base * right.value, HUNDRED) else right.value

    private fun term(): Operand {
        val first = operand()
        if (!peekMultiplicative()) return first
        var value = first.resolved
        while (true) {
            value = when {
                match('*') -> value * operand().resolved
                match('/') -> Evaluator.divide(value, operand().resolved)
                else -> return Operand(value, isPercent = false)
            }
        }
    }

    private fun operand(): Operand {
        val value = unary()
        return Operand(value, isPercent = match('%'))
    }

    private fun peekMultiplicative(): Boolean {
        val token = tokens.getOrNull(position)
        return token is Token.Operator && (token.symbol == '*' || token.symbol == '/')
    }

    private companion object {
        val HUNDRED = BigDecimal.fromInt(100)
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
