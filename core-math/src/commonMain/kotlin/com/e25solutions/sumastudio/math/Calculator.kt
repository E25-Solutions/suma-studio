package com.e25solutions.sumastudio.math

import com.ionspin.kotlin.bignum.decimal.BigDecimal

/** Teclas que entiende la calculadora. La interfaz solo traduce toques a [Key]. */
sealed class Key {
    data class Digit(val digit: Char) : Key() {
        init { require(digit in '0'..'9') }
    }
    data class Operator(val symbol: Char) : Key() {
        init { require(symbol in OPERATORS) }
    }
    data object Decimal : Key()
    data object Percent : Key()
    /** Un solo botón "( )": abre o cierra según el contexto. */
    data object Parentheses : Key()
    data object ToggleSign : Key()
    data object Clear : Key()
    data object Backspace : Key()
    data object Equals : Key()
    data object MemoryClear : Key()
    data object MemoryRecall : Key()
    data object MemoryAdd : Key()
    data object MemorySubtract : Key()
    /** Inserta un número ya calculado, por ejemplo al tocar el historial. */
    data class InsertNumber(val text: String) : Key()
    /** Pega texto del portapapeles; se descarta si no es una expresión válida. */
    data class Paste(val text: String) : Key()

    companion object {
        const val PLUS = '+'
        const val MINUS = '−'
        const val TIMES = '×'
        const val DIVIDE = '÷'
        val OPERATORS = setOf(PLUS, MINUS, TIMES, DIVIDE)
    }
}

data class HistoryEntry(val expression: String, val result: String)

enum class CalcError { DivisionByZero, Syntax }

/**
 * Estado inmutable de la calculadora básica. Cada toque produce un estado nuevo con [press].
 *
 * La expresión usa los símbolos de pantalla (× ÷ −); el motor los entiende tal cual.
 */
data class CalculatorState(
    val expression: String = "",
    val history: List<HistoryEntry> = emptyList(),
    val memory: BigDecimal? = null,
    val error: CalcError? = null,
    /** Verdadero justo después de "=": un dígito nuevo empieza otra cuenta. */
    val showingResult: Boolean = false,
) {
    /** Resultado anticipado mientras se escribe; nulo si aún no hay una operación válida. */
    val preview: String?
        get() {
            if (showingResult || !hasOperation(expression)) return null
            val result = tryEvaluate(closeParentheses(expression)) ?: return null
            return toDisplay(result)
        }

    fun press(key: Key): CalculatorState =
        when (key) {
            is Key.Digit -> appendDigit(key.digit)
            is Key.Operator -> appendOperator(key.symbol)
            Key.Decimal -> appendDecimal()
            Key.Percent -> if (endsWithValue()) edit(expression + "%") else this
            Key.Parentheses -> appendParenthesis()
            Key.ToggleSign -> toggleSign()
            Key.Clear -> copy(expression = "", error = null, showingResult = false)
            Key.Backspace -> if (showingResult) copy(expression = "", showingResult = false) else edit(expression.dropLast(1))
            Key.Equals -> evaluate()
            Key.MemoryClear -> copy(memory = null)
            Key.MemoryRecall -> memory?.let { insertNumber(toDisplay(Evaluator.format(it))) } ?: this
            Key.MemoryAdd -> updateMemory(add = true)
            Key.MemorySubtract -> updateMemory(add = false)
            is Key.InsertNumber -> insertNumber(key.text)
            is Key.Paste -> paste(key.text)
        }

    private fun edit(newExpression: String) =
        copy(expression = newExpression, error = null, showingResult = false)

    private val last: Char? get() = expression.lastOrNull()

    private fun endsWithValue(): Boolean = last?.let { it.isDigit() || it == ')' || it == '%' } ?: false

    private fun appendDigit(digit: Char): CalculatorState {
        if (showingResult) return edit(digit.toString())
        if (last == ')' || last == '%') return edit("$expression$TIMES$digit")
        // Evita ceros a la izquierda: "0" seguido de "5" queda "5".
        val number = currentNumber()
        if (number == "0") return edit(expression.dropLast(1) + digit)
        return edit(expression + digit)
    }

    private fun appendOperator(symbol: Char): CalculatorState {
        val base = if (showingResult) copy(showingResult = false) else this
        val last = base.last
        return when {
            last == null -> if (symbol == MINUS) base.edit("−") else base
            last == '(' -> if (symbol == MINUS) base.edit(base.expression + MINUS) else base
            last in Key.OPERATORS -> {
                val trimmed = base.expression.dropLast(1)
                // Un "−" suelto al inicio o tras "(" no se reemplaza por otro operador.
                if (trimmed.isEmpty() || trimmed.last() == '(') base
                else base.edit(trimmed + symbol)
            }
            last == '.' -> base.edit(base.expression.dropLast(1) + symbol)
            else -> base.edit(base.expression + symbol)
        }
    }

    private fun appendDecimal(): CalculatorState {
        if (showingResult) return edit("0.")
        if (currentNumber().contains('.')) return this
        return when {
            last == null || last in Key.OPERATORS || last == '(' -> edit("${expression}0.")
            last == ')' || last == '%' -> edit("$expression${TIMES}0.")
            else -> edit("$expression.")
        }
    }

    private fun appendParenthesis(): CalculatorState {
        val base = if (showingResult) copy(showingResult = false) else this
        val open = base.expression.count { it == '(' } - base.expression.count { it == ')' }
        return when {
            open > 0 && base.endsWithValue() -> base.edit(base.expression + ")")
            base.endsWithValue() -> base.edit(base.expression + "$TIMES(")
            base.last == '.' -> base
            else -> base.edit(base.expression + "(")
        }
    }

    /** Cambia el signo del número que se está escribiendo (o del resultado). */
    private fun toggleSign(): CalculatorState {
        val base = if (showingResult) copy(showingResult = false) else this
        val expr = base.expression
        if (expr.isEmpty()) return base.edit("−")
        var start = expr.length
        while (start > 0 && (expr[start - 1].isDigit() || expr[start - 1] == '.')) start--
        if (start == expr.length) {
            // No hay número al final: solo se alterna un "−" suelto.
            return when {
                expr.endsWith("−") && isUnaryMinus(expr, expr.length - 1) -> base.edit(expr.dropLast(1))
                last in Key.OPERATORS || last == '(' -> base.edit(expr + MINUS)
                else -> base
            }
        }
        val minusAt = start - 1
        return if (minusAt >= 0 && expr[minusAt] == MINUS && isUnaryMinus(expr, minusAt)) {
            base.edit(expr.removeRange(minusAt, start))
        } else {
            base.edit(expr.substring(0, start) + MINUS + expr.substring(start))
        }
    }

    private fun isUnaryMinus(expr: String, index: Int): Boolean {
        val before = expr.getOrNull(index - 1) ?: return true
        return before in Key.OPERATORS || before == '('
    }

    private fun evaluate(): CalculatorState {
        if (expression.isEmpty() || showingResult) return this
        val complete = closeParentheses(expression.trimEnd { it in Key.OPERATORS || it == '.' || it == '(' })
        if (complete.isEmpty()) return this
        return try {
            val result = toDisplay(Evaluator.evaluateToString(complete))
            copy(
                expression = result,
                history = history + HistoryEntry(complete, result),
                error = null,
                showingResult = true,
            )
        } catch (e: MathError.DivisionByZero) {
            copy(error = CalcError.DivisionByZero)
        } catch (e: MathError) {
            copy(error = CalcError.Syntax)
        }
    }

    private fun updateMemory(add: Boolean): CalculatorState {
        val value = tryEvaluate(closeParentheses(expression))?.let { BigDecimal.parseString(fromDisplay(it)) }
            ?: return this
        val current = memory ?: BigDecimal.ZERO
        return copy(memory = if (add) current + value else current - value)
    }

    private fun insertNumber(text: String): CalculatorState {
        val number = toDisplay(text)
        return when {
            showingResult || expression.isEmpty() -> edit(number)
            last in Key.OPERATORS || last == '(' -> edit(expression + number)
            else -> edit("$expression$TIMES$number")
        }
    }

    private fun paste(text: String): CalculatorState {
        val cleaned = text.filterNot { it.isWhitespace() || it == ',' }
            .replace('*', TIMES).replace('/', DIVIDE).replace('-', MINUS)
        if (cleaned.isEmpty() || cleaned.any { !(it.isDigit() || it in ".()%" || it in Key.OPERATORS) }) return this
        // Solo se acepta si, completado, el motor lo entiende.
        if (tryEvaluate(closeParentheses(cleaned)) == null) return this
        return when {
            showingResult || expression.isEmpty() -> edit(cleaned)
            last in Key.OPERATORS || last == '(' -> edit(expression + cleaned)
            else -> edit("$expression$TIMES$cleaned")
        }
    }

    /** Dígitos y punto del número al final de la expresión. */
    private fun currentNumber(): String = expression.takeLastWhile { it.isDigit() || it == '.' }

    private companion object {
        const val TIMES = Key.TIMES
        const val MINUS = Key.MINUS
        const val DIVIDE = Key.DIVIDE

        fun hasOperation(expr: String): Boolean {
            // Un "−" inicial no cuenta como operación: "−5" no necesita vista previa.
            val body = expr.removePrefix("−")
            return body.any { it in Key.OPERATORS || it == '%' }
        }

        fun closeParentheses(expr: String): String {
            val open = expr.count { it == '(' } - expr.count { it == ')' }
            return if (open > 0) expr + ")".repeat(open) else expr
        }

        fun tryEvaluate(expr: String): String? = try {
            Evaluator.evaluateToString(expr)
        } catch (e: MathError) {
            null
        }

        fun toDisplay(text: String) = text.replace('-', MINUS)
        fun fromDisplay(text: String) = text.replace(MINUS, '-')
    }
}
