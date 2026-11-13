package com.e25solutions.sumastudio.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.e25solutions.sumastudio.math.Key
import com.e25solutions.sumastudio.ui.resources.Res
import com.e25solutions.sumastudio.ui.resources.key_backspace
import com.e25solutions.sumastudio.ui.resources.key_clear
import com.e25solutions.sumastudio.ui.resources.key_decimal
import com.e25solutions.sumastudio.ui.resources.key_divide
import com.e25solutions.sumastudio.ui.resources.key_equals
import com.e25solutions.sumastudio.ui.resources.key_minus
import com.e25solutions.sumastudio.ui.resources.key_parentheses
import com.e25solutions.sumastudio.ui.resources.key_percent
import com.e25solutions.sumastudio.ui.resources.key_plus
import com.e25solutions.sumastudio.ui.resources.key_times
import com.e25solutions.sumastudio.ui.resources.key_toggle_sign
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private enum class Role { Digit, Function, Operator, Equals }

private class Button(
    val label: String,
    val key: Key,
    val role: Role,
    val description: StringResource? = null,
    /** Mantener presionado: "⌫" borra todo. */
    val longPress: Key? = null,
)

private val rows: List<List<Button>> = listOf(
    listOf(
        Button("AC", Key.Clear, Role.Function, Res.string.key_clear),
        Button("( )", Key.Parentheses, Role.Function, Res.string.key_parentheses),
        Button("%", Key.Percent, Role.Function, Res.string.key_percent),
        Button("÷", Key.Operator(Key.DIVIDE), Role.Operator, Res.string.key_divide),
    ),
    listOf(digit('7'), digit('8'), digit('9'), Button("×", Key.Operator(Key.TIMES), Role.Operator, Res.string.key_times)),
    listOf(digit('4'), digit('5'), digit('6'), Button("−", Key.Operator(Key.MINUS), Role.Operator, Res.string.key_minus)),
    listOf(digit('1'), digit('2'), digit('3'), Button("+", Key.Operator(Key.PLUS), Role.Operator, Res.string.key_plus)),
    listOf(
        Button("±", Key.ToggleSign, Role.Digit, Res.string.key_toggle_sign),
        digit('0'),
        Button(".", Key.Decimal, Role.Digit, Res.string.key_decimal),
        Button("=", Key.Equals, Role.Equals, Res.string.key_equals),
    ),
)

private val backspace = Button("⌫", Key.Backspace, Role.Function, Res.string.key_backspace, longPress = Key.Clear)

private fun digit(c: Char) = Button(c.toString(), Key.Digit(c), Role.Digit)

@Composable
fun Keypad(onKey: (Key) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Fila superior: borrar a la derecha, alineado con la columna de operadores.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { Box(Modifier.weight(1f)) }
            KeyButton(backspace, onKey, Modifier.weight(1f).aspectRatio(2f))
        }
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { button ->
                    KeyButton(button, onKey, Modifier.weight(1f).aspectRatio(1.25f))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeyButton(button: Button, onKey: (Key) -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (button.role) {
        Role.Digit -> colors.surfaceVariant.copy(alpha = 0.55f) to colors.onSurface
        Role.Function -> Color.Transparent to colors.primary
        Role.Operator -> colors.secondaryContainer to colors.onSecondaryContainer
        Role.Equals -> colors.primary to colors.onPrimary
    }
    val description = button.description?.let { stringResource(it) } ?: button.label
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = container,
        contentColor = content,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = { onKey(button.key) },
                    onLongClick = button.longPress?.let { long -> { onKey(long) } },
                )
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Text(button.label, fontSize = if (button.label.length > 1) 20.sp else 26.sp)
        }
    }
}
