package com.e25solutions.sumastudio.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Pruebas de referencia. Cada caso se verifica contra una calculadora física
 * antes de agregarse (ver CONTRIBUTING.md).
 */
class EvaluatorTest {

    private fun assertResult(expected: String, expression: String) =
        assertEquals(expected, Evaluator.evaluateToString(expression), "\"$expression\"")

    @Test
    fun decimalesExactos() {
        assertResult("0.3", "0.1 + 0.2")
        assertResult("0.0001", "0.1 × 0.001")
        assertResult("1", "0.7 + 0.1 + 0.2")
    }

    @Test
    fun precedenciaYParentesis() {
        assertResult("14", "2 + 3 × 4")
        assertResult("20", "(2 + 3) × 4")
        assertResult("5", "10 - 2 - 3")
        assertResult("2", "16 ÷ 4 ÷ 2")
    }

    @Test
    fun signoUnario() {
        assertResult("-5", "-5")
        assertResult("5", "-(-5)")
        assertResult("-6", "2 × -3")
    }

    @Test
    fun division() {
        assertResult("0.25", "1 ÷ 4")
        assertResult("3", "6 / 2")
        assertResult("0.3333333333333333333333333333333333", "1 ÷ 3")
    }

    @Test
    fun simbolosAlternos() {
        assertResult("6", "2*3")
        assertResult("1", "3−2")
        assertResult("0.5", ".5")
    }

    @Test
    fun errores() {
        assertFailsWith<MathError.DivisionByZero> { Evaluator.evaluate("1 ÷ 0") }
        assertFailsWith<MathError.Syntax> { Evaluator.evaluate("") }
        assertFailsWith<MathError.Syntax> { Evaluator.evaluate("2 +") }
        assertFailsWith<MathError.Syntax> { Evaluator.evaluate("(2 + 3") }
        assertFailsWith<MathError.Syntax> { Evaluator.evaluate("1.2.3") }
        assertFailsWith<MathError.Syntax> { Evaluator.evaluate("2 x 3") }
    }
}
