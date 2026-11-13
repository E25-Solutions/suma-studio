package com.e25solutions.sumastudio.math

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

/**
 * Evalúa expresiones aritméticas con decimales de precisión arbitraria.
 *
 * Soporta: + - × ÷ (también * y /), paréntesis, signo unario y decimales.
 * Suma, resta y multiplicación son exactas; la división redondea a [PRECISION] dígitos.
 *
 * Es la base de la Fase 1; las funciones científicas se agregan en la Fase 2.
 */
object Evaluator {

    /** Dígitos significativos usados en la división. */
    const val PRECISION = 34L

    private val divisionMode = DecimalMode(PRECISION, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

    /** Evalúa [expression] y devuelve el resultado como número. */
    fun evaluate(expression: String): BigDecimal {
        val parser = Parser(Tokenizer(expression).tokenize())
        return parser.parse()
    }

    /** Evalúa [expression] y devuelve el resultado como texto, sin ceros sobrantes. */
    fun evaluateToString(expression: String): String = format(evaluate(expression))

    /** Da formato decimal plano a [value]: sin notación científica ni ceros al final. */
    fun format(value: BigDecimal): String {
        var text = value.toStringExpanded()
        if (text.contains('.')) {
            text = text.trimEnd('0').trimEnd('.')
        }
        return if (text == "-0") "0" else text
    }

    internal fun divide(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (b.isZero()) throw MathError.DivisionByZero
        return a.divide(b, divisionMode)
    }
}

/** Errores que la interfaz muestra al usuario. */
sealed class MathError(message: String) : Exception(message) {
    data object DivisionByZero : MathError("No se puede dividir entre cero")
    class Syntax(detail: String) : MathError("Error de sintaxis: $detail")
}
