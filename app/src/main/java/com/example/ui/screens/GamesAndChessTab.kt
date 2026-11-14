package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChessPuzzle
import com.example.data.model.MatchPair
import com.example.data.model.SubjectRecord
import com.example.domain.ChessData
import com.example.ui.theme.ChessBlackPiece
import com.example.ui.theme.ChessDarkSquare
import com.example.ui.theme.ChessLightSquare
import com.example.ui.theme.ChessWhitePiece
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.AiCallState

@Composable
fun GamesAndChessTab(
    subjects: List<SubjectRecord>,
    matchPairsState: AiCallState<List<MatchPair>>,
    chessCoachState: AiCallState<String>,
    onGenerateMatchPairs: (String) -> Unit,
    onMinigameWin: (String) -> Unit,
    onAskChessCoach: (ChessPuzzle, String) -> Unit
) {
    var subMode by remember { mutableIntStateOf(0) } // 0 = Minijuego Materias, 1 = Ajedrez

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = subMode == 0,
                    onClick = { subMode = 0 },
                    label = { Text("Minijuego de Materias") },
                    modifier = Modifier.testTag("mode_minigame_chip")
                )
                FilterChip(
                    selected = subMode == 1,
                    onClick = { subMode = 1 },
                    label = { Text("Repaso de Ajedrez") },
                    modifier = Modifier.testTag("mode_chess_chip")
                )
            }
        }

        if (subMode == 0) {
            item {
                MatchMinigameCard(
                    subjects = subjects,
                    matchPairsState = matchPairsState,
                    onGeneratePairs = onGenerateMatchPairs,
                    onWin = { onMinigameWin("Minijuego de Conceptos") }
                )
            }
        } else {
            item {
                ChessTrainingCard(
                    chessCoachState = chessCoachState,
                    onSolvePuzzle = { onMinigameWin("Repaso de Ajedrez") },
                    onAskCoach = onAskChessCoach
                )
            }
        }
    }
}

@Composable
private fun MatchMinigameCard(
    subjects: List<SubjectRecord>,
    matchPairsState: AiCallState<List<MatchPair>>,
    onGeneratePairs: (String) -> Unit,
    onWin: () -> Unit
) {
    var chosenSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "Biología") }
    var selectedTermId by remember { mutableStateOf<String?>(null) }
    val matchedIds = remember { mutableStateListOf<String>() }
    var wrongFlash by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Extension, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Conecta Concepto y Definición", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(
                value = chosenSubject,
                onValueChange = { chosenSubject = it },
                label = { Text("Materia para el minijuego") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    matchedIds.clear()
                    selectedTermId = null
                    wrongFlash = false
                    onGeneratePairs(chosenSubject)
                },
                enabled = chosenSubject.isNotBlank() && matchPairsState !is AiCallState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generar Reto de Materia con IA")
            }

            when (matchPairsState) {
                is AiCallState.Idle -> {
                    Text(
                        text = "La IA crea un tablero interactivo de conceptos clave para reforzar tus materias jugando.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is AiCallState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Preparando tarjetas...")
                    }
                }
                is AiCallState.Error -> {
                    Text(matchPairsState.message, color = MaterialTheme.colorScheme.error)
                }
                is AiCallState.Success -> {
                    val pairs = matchPairsState.data
                    val shuffledDefs = remember(pairs) { pairs.shuffled() }

                    if (wrongFlash) {
                        Text("No coincide, ¡intenta otro par!", color = StatusDanger, style = MaterialTheme.typography.labelLarge)
                    }

                    if (matchedIds.size == pairs.size && pairs.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StatusSuccess.copy(alpha = 0.16f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusSuccess)
                                Spacer(Modifier.width(8.dp))
                                Text("¡Tablero completado! Ganaste XP para tu mascota.", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Conceptos", style = MaterialTheme.typography.labelLarge)
                            pairs.forEach { p ->
                                val isMatched = p.id in matchedIds
                                val isSelected = selectedTermId == p.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        isMatched -> StatusSuccess.copy(alpha = 0.2f)
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isMatched) {
                                            selectedTermId = p.id
                                            wrongFlash = false
                                        }
                                        .padding(10.dp)
                                ) {
                                    Text(p.term, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Definiciones", style = MaterialTheme.typography.labelLarge)
                            shuffledDefs.forEach { d ->
                                val isMatched = d.id in matchedIds
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isMatched) StatusSuccess.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isMatched && selectedTermId != null) {
                                            if (selectedTermId == d.id) {
                                                matchedIds.add(d.id)
                                                selectedTermId = null
                                                wrongFlash = false
                                                if (matchedIds.size == pairs.size) {
                                                    onWin()
                                                }
                                            } else {
                                                wrongFlash = true
                                            }
                                        }
                                        .padding(10.dp)
                                ) {
                                    Text(d.definition, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChessTrainingCard(
    chessCoachState: AiCallState<String>,
    onSolvePuzzle: () -> Unit,
    onAskCoach: (ChessPuzzle, String) -> Unit
) {
    val puzzles = ChessData.builtInPuzzles
    var puzzleIndex by remember { mutableIntStateOf(0) }
    val puzzle = puzzles[puzzleIndex]

    var selectedSquare by remember(puzzle.id) { mutableStateOf<Pair<Int, Int>?>(null) }
    var solved by remember(puzzle.id) { mutableStateOf(false) }
    var feedback by remember(puzzle.id) { mutableStateOf<String?>(null) }
    var coachQuestion by remember { mutableStateOf("") }

    val board = remember(puzzle.id, solved) {
        val b = ChessData.parseFenBoard(puzzle.boardFen)
        if (solved) {
            val (fr, fc) = puzzle.solutionFrom
            val (tr, tc) = puzzle.solutionTo
            b[tr][tc] = b[fr][fc]
            b[fr][fc] = null
        }
        b
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(puzzles.indices.toList()) { idx ->
                    FilterChip(
                        selected = puzzleIndex == idx,
                        onClick = { puzzleIndex = idx },
                        label = { Text(puzzles[idx].title) }
                    )
                }
            }

            Text(
                text = "${puzzle.theme} · Juegan ${puzzle.sideToMove}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(puzzle.prompt, style = MaterialTheme.typography.bodyMedium)

            // 8x8 Interactive Chess Board
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            ) {
                for (r in 0..7) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (c in 0..7) {
                            val isLight = (r + c) % 2 == 0
                            val isSelected = selectedSquare == (r to c)
                            val isTargetSolved = solved && (puzzle.solutionTo == (r to c))
                            val cellColor = when {
                                isTargetSolved -> StatusSuccess.copy(alpha = 0.65f)
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                                isLight -> ChessLightSquare
                                else -> ChessDarkSquare
                            }
                            val piece = board[r][c]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(cellColor)
                                    .clickable(enabled = !solved) {
                                        val currentSel = selectedSquare
                                        if (currentSel == null) {
                                            if (ChessData.isWhitePiece(piece)) {
                                                selectedSquare = r to c
                                                feedback = null
                                            }
                                        } else if (currentSel == (r to c)) {
                                            selectedSquare = null
                                        } else {
                                            if (currentSel == puzzle.solutionFrom && (r to c) == puzzle.solutionTo) {
                                                solved = true
                                                selectedSquare = null
                                                feedback = "¡Correcto! ${puzzle.solutionMoveSan}: ${puzzle.explanation}"
                                                onSolvePuzzle()
                                            } else {
                                                selectedSquare = null
                                                feedback = "Esa no es la mejor jugada táctica. ¡Intenta otra vez!"
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val symbol = ChessData.pieceSymbol(piece)
                                if (symbol.isNotEmpty()) {
                                    Text(
                                        text = symbol,
                                        fontSize = 24.sp,
                                        color = if (ChessData.isWhitePiece(piece)) ChessWhitePiece else ChessBlackPiece,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (feedback != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (solved) StatusSuccess.copy(alpha = 0.16f) else StatusDanger.copy(alpha = 0.14f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = feedback!!,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = {
                        onAskCoach(puzzle, "Dame una pista táctica sin revelar toda la respuesta si aún no resuelvo.")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Pista IA")
                }
                Button(
                    onClick = {
                        solved = true
                        feedback = "Solución: ${puzzle.solutionMoveSan}. ${puzzle.explanation}"
                    },
                    enabled = !solved,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ver Solución")
                }
            }

            OutlinedTextField(
                value = coachQuestion,
                onValueChange = { coachQuestion = it },
                label = { Text("Pregunta al entrenador de ajedrez IA") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            FilledTonalButton(
                onClick = {
                    val q = coachQuestion.ifBlank { "Explícame por qué funciona ${puzzle.solutionMoveSan} en esta posición." }
                    onAskCoach(puzzle, q)
                    coachQuestion = ""
                },
                enabled = chessCoachState !is AiCallState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Consultar Entrenador IA")
            }

            when (chessCoachState) {
                is AiCallState.Idle -> Unit
                is AiCallState.Loading -> CircularProgressIndicator(modifier = Modifier.size(22.dp))
                is AiCallState.Error -> Text(chessCoachState.message, color = MaterialTheme.colorScheme.error)
                is AiCallState.Success -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(chessCoachState.data, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
