package com.e25solutions.sumastudio.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CalculatorTest {

    /** Teclea una secuencia: dígitos, ".", "+−×÷", "%", "(" para paréntesis, "=" y "±". */
    private fun type(keys: String, start: CalculatorState = CalculatorState()): CalculatorState =
        keys.fold(start) { state, c ->
            state.press(
                when (c) {
                    in '0'..'9' -> Key.Digit(c)
                    '.' -> Key.Decimal
                    '+', '−', '×', '÷' -> Key.Operator(c)
                    '%' -> Key.Percent
                    '(' -> Key.Parentheses
                    '=' -> Key.Equals
                    '±' -> Key.ToggleSign
                    'C' -> Key.Clear
                    '<' -> Key.Backspace
                    else -> error("Tecla desconocida: $c")
                }
            )
        }

    @Test
    fun sumaBasica() {
        val state = type("12+30=")
        assertEquals("42", state.expression)
        assertEquals(HistoryEntry("12+30", "42"), state.history.single())
    }

    @Test
    fun vistaPreviaMientrasSeEscribe() {
        assertNull(type("12").preview)
        assertEquals("42", type("12+30").preview)
        assertEquals("20", type("(2+3)×4").preview)
        assertNull(type("12+30=").preview)
    }

    @Test
    fun operadorRepetidoSeReemplaza() {
        assertEquals("5×", type("5+×").expression)
        assertEquals("5", type("5+=").expression)
    }

    @Test
    fun digitoTrasResultadoEmpiezaOtraCuenta() {
        assertEquals("7", type("2+2=7").expression)
        assertEquals("4+1", type("2+2=+1").expression)
    }

    @Test
    fun puntoDecimal() {
        assertEquals("0.5", type(".5").expression)
        assertEquals("1.5", type("1..5").expression)
        assertEquals("2+0.", type("2+.").expression)
    }

    @Test
    fun sinCerosALaIzquierda() {
        assertEquals("5", type("05").expression)
        assertEquals("0.05", type("0.05").expression)
    }

    @Test
    fun parentesisInteligentes() {
        assertEquals("(2+3)", type("(2+3(").expression)
        assertEquals("2×(", type("2(").expression)
        assertEquals("20", type("(2+3(×4=").expression)
        // Los paréntesis abiertos se cierran solos al calcular.
        assertEquals("5", type("(2+3=").expression)
    }

    @Test
    fun cambioDeSigno() {
        assertEquals("−5", type("5±").expression)
        assertEquals("5", type("5±±").expression)
        assertEquals("3×−2", type("3×2±").expression)
        assertEquals("−4", type("2+2=±").expression)
    }

    @Test
    fun porcentaje() {
        assertEquals("55", type("50+10%=").expression)
        assertEquals("45", type("50−10%=").expression)
        assertEquals("5", type("50×10%=").expression)
        assertEquals("0.1", type("10%=").expression)
    }

    @Test
    fun resultadosNegativosUsanSignoDePantalla() {
        val state = type("3−5=")
        assertEquals("−2", state.expression)
        assertEquals("−5", type("×2.5=", state).expression)
    }

    @Test
    fun divisionEntreCeroMuestraError() {
        val state = type("1÷0=")
        assertEquals(CalcError.DivisionByZero, state.error)
        assertEquals("1÷0", state.expression)
        assertNull(type("C", state).error)
    }

    @Test
    fun borrar() {
        assertEquals("12", type("123<").expression)
        assertEquals("", type("12+3=<").expression)
        assertEquals("", type("12+3C").expression)
    }

    @Test
    fun memoria() {
        var state = type("10")
        state = state.press(Key.MemoryAdd)
        state = type("C2+3", state).press(Key.MemoryAdd)
        state = type("C1", state).press(Key.MemorySubtract)
        state = type("C", state).press(Key.MemoryRecall)
        assertEquals("14", state.expression)
        state = type("+", state).press(Key.MemoryRecall)
        assertEquals("14+14", state.expression)
        assertNull(state.press(Key.MemoryClear).memory)
    }

    @Test
    fun reutilizarDelHistorial() {
        val state = type("2+")
        assertEquals("2+42", state.press(Key.InsertNumber("42")).expression)
        assertEquals("2×42", type("2").press(Key.InsertNumber("42")).expression)
    }

    @Test
    fun pegar() {
        assertEquals("1234.5", CalculatorState().press(Key.Paste("1,234.5")).expression)
        assertEquals("2×3", CalculatorState().press(Key.Paste("2*3")).expression)
        // Texto que no es una expresión se ignora.
        assertEquals("", CalculatorState().press(Key.Paste("hola")).expression)
        assertTrue(CalculatorState().press(Key.Paste("2++")).expression.isEmpty())
    }
}
