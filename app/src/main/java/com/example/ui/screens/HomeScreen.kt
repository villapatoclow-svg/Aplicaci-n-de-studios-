package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AiConnectorConfig
import com.example.data.model.ExamResult
import com.example.data.model.SeasonalThemeInfo
import com.example.data.model.SmartTask
import com.example.data.model.StudentProfile
import com.example.data.model.StudyPlan
import com.example.data.model.SubjectRecord
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import java.util.Locale

private data class BentoTileItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    profile: StudentProfile?,
    subjects: List<SubjectRecord>,
    plans: List<StudyPlan>,
    tasks: List<SmartTask>,
    examResults: List<ExamResult>,
    petMessage: String,
    isGeneratingTask: Boolean,
    seasonalInfo: SeasonalThemeInfo,
    connectorConfig: AiConnectorConfig,
    isAdmin: Boolean,
    onRefreshPetMessage: () -> Unit,
    onSaveSubject: (String, Double, Long) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onCreateSmartTask: (String, String) -> Unit,
    onToggleTask: (SmartTask) -> Unit,
    onDeleteTask: (String) -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToPlanner: (Int) -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToAiLab: (Int) -> Unit,
    onNavigateToPlanAlto: () -> Unit,
    onNavigateToConnectors: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onClaimSeasonalEgg: () -> Unit,
    onTriggerProton2Egg: () -> Unit
) {
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var protonBadgeTaps by remember { mutableIntStateOf(0) }

    val weakSubjects = remember(subjects) { subjects.filter { it.priority == "reforzar" } }
    val avgGrade = remember(subjects) {
        if (subjects.isEmpty()) 0.0 else subjects.map { it.grade }.average()
    }
    val completedTasksCount = remember(tasks) { tasks.count { it.completed } }
    val isGamified = profile?.sessionMode != "numeric"

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Welcome Banner + Seasonal Animation + Secret Protón 2 Easter Egg
            item {
                val snowAnim = rememberInfiniteTransition(label = "snow_fall")
                val snowPhase by snowAnim.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "snow_phase"
                )
                Card(
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.height(176.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_study),
                            contentDescription = "Banner de estudio MentePro",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                                        )
                                    )
                                )
                        )
                        if (seasonalInfo.isChristmasMode) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val flakes = 18
                                for (i in 0 until flakes) {
                                    val fx = ((i * 59f + 23f) % 100f) / 100f * size.width
                                    val fy = (((snowPhase + i * 0.13f) % 1f)) * size.height
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.72f),
                                        radius = if (i % 2 == 0) 4.5f else 3f,
                                        center = Offset(fx, fy)
                                    )
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Hola, ${profile?.displayName ?: "Estudiante"}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier
                                            .testTag("proton2_easter_egg_badge")
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                protonBadgeTaps += 1
                                                if (protonBadgeTaps >= 5) {
                                                    protonBadgeTaps = 0
                                                    onTriggerProton2Egg()
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (seasonalInfo.isChristmasMode) Icons.Filled.AcUnit else Icons.Filled.Bolt,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Protón 2",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (weakSubjects.isNotEmpty()) {
                                        "Detectamos ${weakSubjects.size} materia(s) por reforzar · ${seasonalInfo.badgeText}"
                                    } else {
                                        "Dashboard Protón 2 listo · ${seasonalInfo.badgeText}"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onNavigateToAiLab(1) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("quick_camera_homework_button")
                                ) {
                                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Cámara Tareas", style = MaterialTheme.typography.labelLarge)
                                }
                                FilledTonalButton(
                                    onClick = onNavigateToCalculator,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("quick_calc_button")
                                ) {
                                    Icon(Icons.Filled.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Calc IA", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }

            // 1.5 Dashboard KPI Bento Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricBox(
                            label = "Promedio",
                            value = String.format(Locale.US, "%.1f", avgGrade),
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = "Tareas Listas",
                            value = "$completedTasksCount/${tasks.size}",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = "Racha",
                            value = "${profile?.streakDays ?: 1}d",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onNavigateToConnectors)
                            .testTag("dashboard_connector_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("Motor IA: ${connectorConfig.activeLabel()}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Text("Toca para conectar Gemini API, Cloud Code u otros", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text("Configurar", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 1.8 Clima / Navidad Huevecillo Easter Egg Banner
            item {
                val eggClaimed = profile?.unlockedBadges?.contains("huevo_${seasonalInfo.seasonId}") == true
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (seasonalInfo.isChristmasMode) Icons.Filled.AcUnit else Icons.Filled.WbSunny,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(seasonalInfo.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (eggClaimed) "${seasonalInfo.eggName} desbloqueado" else "${seasonalInfo.eggName}: ${seasonalInfo.eggRewardNote}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (!eggClaimed) {
                            Button(
                                onClick = onClaimSeasonalEgg,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("home_claim_egg_button")
                            ) {
                                Icon(Icons.Filled.Egg, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Abrir")
                            }
                        } else {
                            TextButton(onClick = { onNavigateToAiLab(3) }) {
                                Text("Clima")
                            }
                        }
                    }
                }
            }

            // 2. Dashboard Bento Grid de Módulos
            item {
                val tiles = buildList {
                    if (isAdmin) {
                        add(
                            BentoTileItem(
                                icon = Icons.Filled.AdminPanelSettings,
                                title = "Modo Admin",
                                subtitle = "Checar usuarios y borrar",
                                testTag = "bento_admin_panel",
                                onClick = onNavigateToAdmin
                            )
                        )
                    }
                    add(BentoTileItem(Icons.Filled.Pets, "Autosauro y Mascota", "Base, accesorios y clima", "bento_pet", { onNavigateToAiLab(3) }))
                    add(BentoTileItem(Icons.Filled.Key, "Claves API & Cloud Code", "Gemini, Claude y más", "bento_connectors", onNavigateToConnectors))
                    add(BentoTileItem(Icons.Filled.BusinessCenter, "Plan Alto 18+", "Finanzas, rutas y asistente", "bento_plan_alto", onNavigateToPlanAlto))
                    add(BentoTileItem(Icons.Filled.Code, "Código y Juegos IA", "Crea apps al instante", "bento_code", { onNavigateToAiLab(2) }))
                    add(BentoTileItem(Icons.Filled.SmartToy, "Play Store y Planes", "$25 Extra, $250 y Pro", "bento_billing", { onNavigateToPlanner(1) }))
                    add(BentoTileItem(Icons.Filled.Psychology, "Tutora IA", "Explicaciones paso a paso", "bento_tutor", { onNavigateToAiLab(0) }))
                    add(BentoTileItem(Icons.Filled.School, "Mi Secundaria", "Noticias y profesores", "bento_school", onNavigateToSubjects))
                    add(BentoTileItem(Icons.Filled.SportsKabaddi, "Duelos IA", "Enfréntate a agentes", "bento_duels", { onNavigateToAiLab(5) }))
                    add(BentoTileItem(Icons.Filled.Storefront, "Workshop", "Descarga planes y guías", "bento_workshop", { onNavigateToPlanner(2) }))
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Dashboard de Módulos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    tiles.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { tile ->
                                BentoDashboardTile(
                                    tile = tile,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 3. Session Mode Card: Mascota Gamificada OR Resumen Numérico
            item {
                if (isGamified) {
                    PetMascotCard(
                        profile = profile,
                        seasonId = seasonalInfo.seasonId,
                        petMessage = petMessage,
                        onRefreshPetMessage = onRefreshPetMessage,
                        onOpenPetConfigurator = { onNavigateToAiLab(3) }
                    )
                } else {
                    NumericMetricsCard(
                        avgGrade = avgGrade,
                        weakCount = weakSubjects.size,
                        completedTasks = completedTasksCount,
                        totalTasks = tasks.size,
                        activePlans = plans.size,
                        examsTaken = examResults.size
                    )
                }
            }

            // 4. Subjects Diagnostic Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Diagnóstico de Materias",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Detecta materias bajas y agrégalas a tu plan IA",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalButton(
                            onClick = { showAddSubjectDialog = true },
                            modifier = Modifier.testTag("add_subject_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Materia")
                        }
                    }

                    if (subjects.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Agrega tus materias y calificaciones para detectar qué reforzar.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { showAddSubjectDialog = true }) {
                                    Text("Agregar")
                                }
                            }
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(subjects, key = { it.id }) { subject ->
                                SubjectChipCard(
                                    subject = subject,
                                    onDelete = { onDeleteSubject(subject.id) }
                                )
                            }
                        }
                        if (weakSubjects.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = StatusWarning.copy(alpha = 0.14f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToPlanner(0) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.TrendingDown,
                                            contentDescription = null,
                                            tint = StatusWarning
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = "Prioridad de refuerzo: ${weakSubjects.joinToString { it.name }}. Toca para crear plan IA.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            // 5. Smart Tasks Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tareas Inteligentes",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Pide tu tarea y la IA genera el procedimiento completo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showAddTaskDialog = true },
                        enabled = !isGeneratingTask,
                        modifier = Modifier.testTag("add_smart_task_button")
                    ) {
                        if (isGeneratingTask) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("Crear con IA")
                    }
                }
            }

            if (tasks.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.PhotoCamera,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Aún no tienes tareas inteligentes",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Describe lo que necesitas (ej. '10 ejercicios de fracciones paso a paso') y la IA lo preparará por ti.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    SmartTaskCard(
                        task = task,
                        onToggle = { onToggleTask(task) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }

    if (showAddSubjectDialog) {
        AddSubjectDialog(
            onDismiss = { showAddSubjectDialog = false },
            onSave = { name, grade, hours ->
                onSaveSubject(name, grade, hours)
                showAddSubjectDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        CreateSmartTaskDialog(
            subjects = subjects.map { it.name },
            onDismiss = { showAddTaskDialog = false },
            onCreate = { prompt, subject ->
                onCreateSmartTask(prompt, subject)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
private fun BentoDashboardTile(
    tile: BentoTileItem,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .testTag(tile.testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = tile.onClick)
            .minimumInteractiveComponentSize()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = tile.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tile.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tile.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PetMascotCard(
    profile: StudentProfile?,
    seasonId: String,
    petMessage: String,
    onRefreshPetMessage: () -> Unit,
    onOpenPetConfigurator: () -> Unit
) {
    val petLevel = profile?.petLevel ?: 1L
    val petXp = profile?.petXp ?: 0L
    val maxXp = (profile?.xpForNextLevel ?: 100L).coerceAtLeast(1L)
    val progress = (petXp.toFloat() / maxXp.toFloat()).coerceIn(0f, 1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PetAvatarCanvas(
                    species = profile?.petSpecies ?: "buho",
                    accessory = profile?.petAccessory ?: "ninguno",
                    accentKey = profile?.petColorAccent ?: "indigo",
                    seasonId = seasonId,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${profile?.petName ?: "Bubo"} · Nv. $petLevel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = profile?.petStageTitle ?: "Explorador",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$petXp / $maxXp XP",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Racha: ${profile?.streakDays ?: 1}d · ${profile?.studyCoins ?: 50} monedas",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = petMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onRefreshPetMessage,
                        modifier = Modifier.testTag("pet_coach_button")
                    ) {
                        Text("Consejo IA")
                    }
                }
            }

            OutlinedButton(
                onClick = onOpenPetConfigurator,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_pet_configurator_button")
            ) {
                Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Personalizar Mascota y Gamificación")
            }
        }
    }
}

@Composable
private fun NumericMetricsCard(
    avgGrade: Double,
    weakCount: Int,
    completedTasks: Int,
    totalTasks: Int,
    activePlans: Int,
    examsTaken: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Sesión Numérica · Rendimiento Académico",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBox(
                    label = "Promedio",
                    value = String.format(Locale.US, "%.1f", avgGrade),
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "A reforzar",
                    value = "$weakCount",
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "Tareas",
                    value = "$completedTasks/$totalTasks",
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "Exámenes",
                    value = "$examsTaken",
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "Planes activos: $activePlans · Métricas optimizadas sin distracciones.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SubjectChipCard(
    subject: SubjectRecord,
    onDelete: () -> Unit
) {
    val badgeColor = when (subject.priority) {
        "reforzar" -> StatusDanger
        "fuerte" -> StatusSuccess
        else -> StatusWarning
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.width(170.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Eliminar materia",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f / 10", subject.grade),
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.16f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (subject.priority == "reforzar") Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = subject.priority.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = badgeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartTaskCard(
    task: SmartTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("toggle_task_${task.id}")
                ) {
                    Icon(
                        imageVector = if (task.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = if (task.completed) "Completada" else "Marcar como completada",
                        tint = if (task.completed) StatusSuccess else MaterialTheme.colorScheme.primary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = "${task.subject} · +${task.xpReward} XP · ${task.dueDateLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Ver procedimiento"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Eliminar tarea")
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (task.procedureSteps.isNotEmpty()) {
                        Text(
                            text = "Procedimiento sugerido:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        task.procedureSteps.forEachIndexed { idx, step ->
                            Text(
                                text = "${idx + 1}. $step",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var grade by remember { mutableFloatStateOf(7.0f) }
    var hours by remember { mutableFloatStateOf(4f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Materia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Álgebra, Biología)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Column {
                    Text(
                        text = "Calificación actual: ${String.format(Locale.US, "%.1f", grade)} (${SubjectRecord.computePriority(grade.toDouble())})",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = grade,
                        onValueChange = { grade = it },
                        valueRange = 0f..10f
                    )
                }
                Column {
                    Text(
                        text = "Meta semanal: ${hours.toInt()} horas",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = hours,
                        onValueChange = { hours = it },
                        valueRange = 1f..20f
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, grade.toDouble(), hours.toLong()) },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun CreateSmartTaskDialog(
    subjects: List<String>,
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull() ?: "Matemáticas") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea Inteligente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Describe más o menos la tarea y qué procedimiento necesitas. La IA la estructurará al instante.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = selectedSubject,
                    onValueChange = { selectedSubject = it },
                    label = { Text("Materia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (subjects.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(subjects) { subj ->
                            AssistChip(
                                onClick = { selectedSubject = subj },
                                label = { Text(subj) }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("¿Qué tarea necesitas?") },
                    placeholder = { Text("Ej. Explicar el teorema de Pitágoras con 3 ejemplos resueltos") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(prompt, selectedSubject) },
                enabled = prompt.isNotBlank()
            ) {
                Text("Generar con IA")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
