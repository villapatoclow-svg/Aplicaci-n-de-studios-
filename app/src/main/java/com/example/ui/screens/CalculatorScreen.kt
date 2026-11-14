package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.StepByStepSolution
import com.example.domain.MathEvaluator
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.viewmodel.AiCallState

@Composable
fun CalculatorScreen(
    calcSolutionState: AiCallState<StepByStepSolution>,
    onSolveWithAi: (String, String) -> Unit
) {
    val modes = listOf("Científica", "Física y Química", "Álgebra y Cálculo")
    var selectedMode by remember { mutableStateOf(modes.first()) }
    var expression by remember { mutableStateOf("") }
    var sciencePrompt by remember { mutableStateOf("") }
    var useDegrees by remember { mutableStateOf(true) }
    var localResult by remember { mutableStateOf("0") }
    var localSteps by remember { mutableStateOf<List<String>>(emptyList()) }
    var localError by remember { mutableStateOf<String?>(null) }

    fun evaluateLocal(expr: String) {
        if (expr.isBlank()) {
            localResult = "0"
            localSteps = emptyList()
            localError = null
            return
        }
        MathEvaluator.evaluate(expr, useDegrees)
            .onSuccess {
                localResult = it.formattedResult
                localSteps = it.steps
                localError = null
            }
            .onFailure {
                localError = it.message
            }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Calculadora Científica con Procedimientos",
                        style = MaterialTheme.typography.titleLarge
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(modes) { mode ->
                            FilterChip(
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode },
                                label = { Text(mode) }
                            )
                        }
                    }
                }
            }

            // Display Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = useDegrees,
                                onClick = {
                                    useDegrees = !useDegrees
                                    evaluateLocal(expression)
                                },
                                label = { Text(if (useDegrees) "DEG" else "RAD") }
                            )
                            Text(
                                text = selectedMode,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = expression.ifBlank { "Ingresa una operación..." },
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = if (expression.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = if (localError != null) localError!! else "= $localResult",
                            style = MaterialTheme.typography.displaySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = if (localError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("calculator_result_text")
                        )

                        if (localSteps.isNotEmpty() && localError == null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("Pasos rápidos:", style = MaterialTheme.typography.labelLarge)
                                    localSteps.forEachIndexed { idx, step ->
                                        Text("${idx + 1}. $step", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Scientific Keypad
            item {
                val rows = listOf(
                    listOf("sin(", "cos(", "tan(", "√(", "^"),
                    listOf("log(", "ln(", "(", ")", "!"),
                    listOf("7", "8", "9", "÷", "π"),
                    listOf("4", "5", "6", "×", "e"),
                    listOf("1", "2", "3", "-", "%"),
                    listOf("C", "0", ".", "+", "=")
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (expression.isNotEmpty()) {
                                    expression = expression.dropLast(1)
                                    evaluateLocal(expression)
                                }
                            },
                            modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Borrar")
                            Spacer(Modifier.width(6.dp))
                            Text("Borrar")
                        }
                        FilledTonalButton(
                            onClick = {
                                val target = expression.ifBlank { sciencePrompt }
                                onSolveWithAi(target, selectedMode)
                            },
                            modifier = Modifier
                                .weight(2f)
                                .minimumInteractiveComponentSize()
                                .testTag("calc_ai_steps_button")
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Procedimiento IA")
                        }
                    }

                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { key ->
                                val isEquals = key == "="
                                val isClear = key == "C"
                                Button(
                                    onClick = {
                                        when {
                                            isClear -> {
                                                expression = ""
                                                evaluateLocal("")
                                            }
                                            isEquals -> evaluateLocal(expression)
                                            else -> {
                                                expression += key
                                                evaluateLocal(expression)
                                            }
                                        }
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = if (isEquals) {
                                        androidx.compose.material3.ButtonDefaults.buttonColors()
                                    } else if (isClear) {
                                        androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    } else {
                                        androidx.compose.material3.ButtonDefaults.filledTonalButtonColors()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("calc_key_$key")
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Science / Algebra Problem Input for AI Step-by-Step Solver
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Resolver Problema de Ciencias o Álgebra",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        OutlinedTextField(
                            value = sciencePrompt,
                            onValueChange = { sciencePrompt = it },
                            label = { Text("Ej. v0=5 m/s, a=2 m/s², t=4 s; calcula distancia") },
                            placeholder = { Text("También puedes escribir ecuaciones, balanceo químico o derivadas") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { onSolveWithAi(sciencePrompt, selectedMode) },
                            enabled = sciencePrompt.isNotBlank() && calcSolutionState !is AiCallState.Loading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Calcular con Procedimiento Completo (1 Ticket)")
                        }
                    }
                }
            }

            // AI Solution Display
            item {
                when (calcSolutionState) {
                    is AiCallState.Idle -> Unit
                    is AiCallState.Loading -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                Text("Desarrollando procedimiento paso a paso...")
                            }
                        }
                    }
                    is AiCallState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = calcSolutionState.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    is AiCallState.Success -> {
                        val sol = calcSolutionState.data
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${sol.subjectArea} · Resultado: ${sol.finalAnswer}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Fórmula: ${sol.formulaUsed}",
                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Text("Procedimiento:", style = MaterialTheme.typography.labelLarge)
                                sol.steps.forEachIndexed { idx, step ->
                                    Text("${idx + 1}. $step", style = MaterialTheme.typography.bodyMedium)
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "Tip: ${sol.studyTip}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}
