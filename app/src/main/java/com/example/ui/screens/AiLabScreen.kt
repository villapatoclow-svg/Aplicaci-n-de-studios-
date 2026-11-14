package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AiDuelAgent
import com.example.data.model.ChessPuzzle
import com.example.data.model.CodePlaygroundResult
import com.example.data.model.DuelChallenge
import com.example.data.model.ExamResult
import com.example.data.model.GamificationMission
import com.example.data.model.GeneratedExam
import com.example.data.model.HomeworkScanResult
import com.example.data.model.MatchPair
import com.example.data.model.SeasonalThemeInfo
import com.example.data.model.SlideAnalysisResult
import com.example.data.model.StepByStepSolution
import com.example.data.model.StudentProfile
import com.example.data.model.SubjectRecord
import com.example.data.model.TutorMessage
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.AiCallState
import com.example.ui.viewmodel.DuelSessionState

@Composable
fun AiLabScreen(
    profile: StudentProfile?,
    subjects: List<SubjectRecord>,
    missions: List<GamificationMission>,
    seasonalInfo: SeasonalThemeInfo,
    tutorMessages: List<TutorMessage>,
    isTutorStreaming: Boolean,
    duelSession: DuelSessionState,
    duelChallengeState: AiCallState<DuelChallenge>,
    examState: AiCallState<GeneratedExam>,
    examResults: List<ExamResult>,
    homeworkScanState: AiCallState<HomeworkScanResult>,
    slideAnalysisState: AiCallState<SlideAnalysisResult>,
    codePlaygroundState: AiCallState<CodePlaygroundResult>,
    calcSolutionState: AiCallState<StepByStepSolution>,
    matchPairsState: AiCallState<List<MatchPair>>,
    chessCoachState: AiCallState<String>,
    initialSubTab: Int = 0,
    onSendTutorMessage: (String) -> Unit,
    onScanHomework: (Bitmap, String) -> Unit,
    onSaveHomeworkTask: (HomeworkScanResult) -> Unit,
    onAnalyzeSlide: (Bitmap, String) -> Unit,
    onSaveSlideTask: (SlideAnalysisResult) -> Unit,
    onGeneratePlayableCode: (String, String) -> Unit,
    onUpdatePetConfig: (String, String, String, String, Long) -> Unit,
    onApplyBaseConfig: (String) -> Unit,
    onFeedPet: () -> Unit,
    onUnlockBadge: (String, String) -> Unit,
    onSelectSeason: (String) -> Unit,
    onClaimSeasonalEgg: () -> Unit,
    onSolveWithAiCalc: (String, String) -> Unit,
    onStartDuel: (AiDuelAgent) -> Unit,
    onSubmitDuelAnswer: (Int) -> Unit,
    onNextDuelRound: () -> Unit,
    onResetDuel: () -> Unit,
    onGenerateExam: (String, String, String, Int) -> Unit,
    onSubmitExamResult: (GeneratedExam, Int) -> Unit,
    onResetExam: () -> Unit,
    onGenerateMatchPairs: (String) -> Unit,
    onMinigameWin: (String) -> Unit,
    onAskChessCoach: (ChessPuzzle, String) -> Unit
) {
    val tabs = listOf(
        "Tutora",
        "Cámara Tareas",
        "Código y Juegos",
        "Mascota y Retos",
        "Calc IA",
        "Duelos IA",
        "Exámenes",
        "Juegos"
    )
    var selectedTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab.coerceIn(0, tabs.lastIndex)) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, maxLines = 1) },
                    modifier = Modifier.testTag("ailab_tab_$index")
                )
            }
        }

        Box(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> TutorTab(
                    messages = tutorMessages,
                    isStreaming = isTutorStreaming,
                    onSendMessage = onSendTutorMessage
                )
                1 -> CameraHomeworkAndSlidesTab(
                    homeworkScanState = homeworkScanState,
                    slideAnalysisState = slideAnalysisState,
                    onScanHomework = onScanHomework,
                    onSaveHomeworkTask = onSaveHomeworkTask,
                    onAnalyzeSlide = onAnalyzeSlide,
                    onSaveSlideTask = onSaveSlideTask
                )
                2 -> CodePlaygroundTab(
                    codeState = codePlaygroundState,
                    onGenerateCode = onGeneratePlayableCode
                )
                3 -> PetAndGamificationTab(
                    profile = profile,
                    missions = missions,
                    seasonalInfo = seasonalInfo,
                    onUpdatePetConfig = onUpdatePetConfig,
                    onApplyBaseConfig = onApplyBaseConfig,
                    onFeedPet = onFeedPet,
                    onUnlockBadge = onUnlockBadge,
                    onSelectSeason = onSelectSeason,
                    onClaimSeasonalEgg = onClaimSeasonalEgg
                )
                4 -> CalculatorScreen(
                    calcSolutionState = calcSolutionState,
                    onSolveWithAi = onSolveWithAiCalc
                )
                5 -> AiDuelsTab(
                    duelSession = duelSession,
                    duelChallengeState = duelChallengeState,
                    onStartDuel = onStartDuel,
                    onSubmitAnswer = onSubmitDuelAnswer,
                    onNextRound = onNextDuelRound,
                    onResetDuel = onResetDuel
                )
                6 -> ExamsTab(
                    subjects = subjects,
                    examState = examState,
                    examResults = examResults,
                    onGenerateExam = onGenerateExam,
                    onSubmitExam = onSubmitExamResult,
                    onResetExam = onResetExam
                )
                7 -> GamesAndChessTab(
                    subjects = subjects,
                    matchPairsState = matchPairsState,
                    chessCoachState = chessCoachState,
                    onGenerateMatchPairs = onGenerateMatchPairs,
                    onMinigameWin = onMinigameWin,
                    onAskChessCoach = onAskChessCoach
                )
            }
        }
    }
}

@Composable
private fun TutorTab(
    messages: List<TutorMessage>,
    isStreaming: Boolean,
    onSendMessage: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quickPrompts = listOf(
                "Explícame el teorema de Pitágoras fácil",
                "Dame un resumen de la célula eucariota",
                "¿Cómo balancear ecuaciones químicas?"
            )
            items(quickPrompts) { prompt ->
                FilterChip(
                    selected = false,
                    onClick = { onSendMessage(prompt) },
                    label = { Text(prompt) }
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isFromUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (msg.isFromUser) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        tonalElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 310.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.text.ifBlank { "Pensando..." },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (msg.isFromUser) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Pregúntale a tu Tutora IA...") },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    val toSend = input
                    input = ""
                    onSendMessage(toSend)
                },
                enabled = input.isNotBlank() && !isStreaming,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("tutor_send_button")
            ) {
                if (isStreaming) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar mensaje")
                }
            }
        }
    }
}

@Composable
private fun ExamsTab(
    subjects: List<SubjectRecord>,
    examState: AiCallState<GeneratedExam>,
    examResults: List<ExamResult>,
    onGenerateExam: (String, String, String, Int) -> Unit,
    onSubmitExam: (GeneratedExam, Int) -> Unit,
    onResetExam: () -> Unit
) {
    var subject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "Matemáticas") }
    var topic by remember { mutableStateOf("Ecuaciones y porcentajes") }
    var difficulty by remember { mutableStateOf("intermedio") }
    var questionCount by remember { mutableFloatStateOf(5f) }

    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var submitted by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (examState) {
            is AiCallState.Idle, is AiCallState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Generador de Exámenes y Miniexámenes", style = MaterialTheme.typography.titleLarge)
                            OutlinedTextField(
                                value = subject,
                                onValueChange = { subject = it },
                                label = { Text("Materia") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = topic,
                                onValueChange = { topic = it },
                                label = { Text("Tema específico") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("basico", "intermedio", "avanzado").forEach { diff ->
                                    FilterChip(
                                        selected = difficulty == diff,
                                        onClick = { difficulty = diff },
                                        label = { Text(diff.replaceFirstChar { it.uppercase() }) }
                                    )
                                }
                            }
                            Text("Preguntas: ${questionCount.toInt()}", style = MaterialTheme.typography.labelLarge)
                            Slider(
                                value = questionCount,
                                onValueChange = { questionCount = it },
                                valueRange = 3f..8f,
                                steps = 4
                            )
                            if (examState is AiCallState.Error) {
                                Text(examState.message, color = MaterialTheme.colorScheme.error)
                            }
                            Button(
                                onClick = {
                                    selectedAnswers.clear()
                                    submitted = false
                                    onGenerateExam(subject, topic, difficulty, questionCount.toInt())
                                },
                                enabled = subject.isNotBlank() && topic.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_exam_button")
                            ) {
                                Icon(Icons.Filled.Quiz, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Crear Examen con IA")
                            }
                        }
                    }
                }
            }

            is AiCallState.Loading -> {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Diseñando preguntas de nivel $difficulty...")
                        }
                    }
                }
            }

            is AiCallState.Success -> {
                val exam = examState.data
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(exam.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${exam.subject} · ${exam.questions.size} preguntas · ${exam.difficulty}")
                        }
                    }
                }

                items(exam.questions.size) { qIdx ->
                    val q = exam.questions[qIdx]
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${qIdx + 1}. ${q.question}", style = MaterialTheme.typography.titleMedium)
                            q.options.forEachIndexed { optIdx, optText ->
                                val isChosen = selectedAnswers[qIdx] == optIdx
                                val isCorrect = q.correctIndex == optIdx
                                val rowBg = when {
                                    !submitted -> MaterialTheme.colorScheme.surface
                                    isCorrect -> StatusSuccess.copy(alpha = 0.16f)
                                    isChosen -> StatusDanger.copy(alpha = 0.16f)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(rowBg)
                                        .clickable(enabled = !submitted) { selectedAnswers[qIdx] = optIdx }
                                        .padding(6.dp)
                                ) {
                                    RadioButton(
                                        selected = isChosen,
                                        onClick = if (!submitted) { { selectedAnswers[qIdx] = optIdx } } else null
                                    )
                                    Text(optText, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            if (submitted) {
                                Text(
                                    text = "Explicación: ${q.explanation}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                item {
                    if (!submitted) {
                        Button(
                            onClick = {
                                submitted = true
                                val correct = exam.questions.indices.count { i ->
                                    selectedAnswers[i] == exam.questions[i].correctIndex
                                }
                                onSubmitExam(exam, correct)
                            },
                            enabled = selectedAnswers.size == exam.questions.size,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Calificar y Guardar")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onResetExam,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Crear otro examen")
                        }
                    }
                }
            }
        }

        if (examResults.isNotEmpty()) {
            item {
                Text("Historial de Exámenes (${examResults.size})", style = MaterialTheme.typography.titleMedium)
            }
            items(examResults.take(5), key = { it.id }) { res ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(res.title, style = MaterialTheme.typography.labelLarge)
                            Text("${res.subject} · ${res.correctAnswers}/${res.totalQuestions} aciertos", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = "${res.score}%",
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = if (res.score >= 70) StatusSuccess else StatusDanger
                        )
                    }
                }
            }
        }
    }
}
