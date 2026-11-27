package com.e25solutions.sumastudio.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.e25solutions.sumastudio.math.CalcError
import com.e25solutions.sumastudio.math.CalculatorState
import com.e25solutions.sumastudio.math.Evaluator
import com.e25solutions.sumastudio.math.Key
import com.e25solutions.sumastudio.ui.resources.Res
import com.e25solutions.sumastudio.ui.resources.action_clear_history
import com.e25solutions.sumastudio.ui.resources.action_copy
import com.e25solutions.sumastudio.ui.resources.action_more
import com.e25solutions.sumastudio.ui.resources.action_paste
import com.e25solutions.sumastudio.ui.resources.action_vibration_off
import com.e25solutions.sumastudio.ui.resources.action_vibration_on
import com.e25solutions.sumastudio.ui.resources.error_division_by_zero
import com.e25solutions.sumastudio.ui.resources.error_syntax
import com.e25solutions.sumastudio.ui.resources.history_empty
import com.e25solutions.sumastudio.ui.resources.memory_indicator
import org.jetbrains.compose.resources.stringResource

@Suppress("DEPRECATION") // LocalClipboardManager: la API nueva es suspendible y aún no es necesaria aquí.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorScreen() {
    var state by remember { mutableStateOf(CalculatorState()) }
    var vibration by remember { mutableStateOf(true) }
    var menuOpen by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val clipboard = LocalClipboardManager.current

    val press: (Key) -> Unit = { key ->
        if (vibration) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        state = state.press(key)
    }

    Column(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // Historial: lo más reciente queda abajo, junto a la pantalla.
        val listState = rememberLazyListState()
        LaunchedEffect(state.history.size) { listState.scrollToItem(0) }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.history.isEmpty()) {
                Text(
                    stringResource(Res.string.history_empty),
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(state = listState, reverseLayout = true, modifier = Modifier.fillMaxSize()) {
                    items(state.history.asReversed()) { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { press(Key.InsertNumber(entry.result)) }
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.End,
                        ) {
                            Text(
                                entry.expression,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(entry.result, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }

        // Pantalla: expresión, vista previa y mensajes.
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = {}, onLongClick = { menuOpen = true }),
                horizontalAlignment = Alignment.End,
            ) {
                val memory = state.memory
                Text(
                    if (memory != null) stringResource(Res.string.memory_indicator, Evaluator.format(memory)) else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    state.expression.ifEmpty { "0" },
                    modifier = Modifier.horizontalScroll(rememberScrollState(), reverseScrolling = true),
                    fontSize = displaySize(state.expression.length),
                    fontWeight = if (state.showingResult) FontWeight.Medium else FontWeight.Light,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                )
                val message = when (state.error) {
                    CalcError.DivisionByZero -> stringResource(Res.string.error_division_by_zero)
                    CalcError.Syntax -> stringResource(Res.string.error_syntax)
                    null -> state.preview ?: ""
                }
                Text(
                    message,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (state.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.action_copy)) },
                    onClick = {
                        clipboard.setText(AnnotatedString(state.expression.replace('−', '-')))
                        menuOpen = false
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.action_paste)) },
                    onClick = {
                        clipboard.getText()?.text?.let { press(Key.Paste(it)) }
                        menuOpen = false
                    },
                )
            }
        }

        // Memoria y opciones.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MemoryButton("MC") { press(Key.MemoryClear) }
            MemoryButton("MR") { press(Key.MemoryRecall) }
            MemoryButton("M+") { press(Key.MemoryAdd) }
            MemoryButton("M−") { press(Key.MemorySubtract) }
            Box {
                var optionsOpen by remember { mutableStateOf(false) }
                TextButton(onClick = { optionsOpen = true }) {
                    Text("⋯", fontSize = 20.sp)
                }
                OptionsMenu(
                    expanded = optionsOpen,
                    vibration = vibration,
                    onDismiss = { optionsOpen = false },
                    onToggleVibration = { vibration = !vibration; optionsOpen = false },
                    onClearHistory = { state = state.copy(history = emptyList()); optionsOpen = false },
                )
            }
        }

        Keypad(onKey = press, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun MemoryButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun OptionsMenu(
    expanded: Boolean,
    vibration: Boolean,
    onDismiss: () -> Unit,
    onToggleVibration: () -> Unit,
    onClearHistory: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = {
                Text(stringResource(if (vibration) Res.string.action_vibration_on else Res.string.action_vibration_off))
            },
            onClick = onToggleVibration,
        )
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.action_clear_history)) },
            onClick = onClearHistory,
        )
    }
}

/** El texto se achica conforme crece la expresión, para que quepa sin desplazar. */
private fun displaySize(length: Int) = when {
    length <= 9 -> 56.sp
    length <= 13 -> 44.sp
    length <= 18 -> 34.sp
    else -> 28.sp
}
