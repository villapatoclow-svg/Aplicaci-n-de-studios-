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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.PlayBillingManager
import com.example.data.model.PlayProductOfferUi
import com.example.data.model.StudentProfile
import com.example.data.model.StudyPlan
import com.example.data.model.SubjectRecord
import com.example.data.model.WorkshopItem
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.AiCallState

@Composable
fun PlannerScreen(
    profile: StudentProfile?,
    subjects: List<SubjectRecord>,
    plans: List<StudyPlan>,
    workshopItems: List<WorkshopItem>,
    playOffers: List<PlayProductOfferUi>,
    isPlayStoreReady: Boolean,
    isGeneratingPlan: Boolean,
    automatedAgentState: AiCallState<String>,
    initialSubTab: Int = 0,
    onSaveSubject: (String, Double, Long) -> Unit,
    onGeneratePlan: (String, Int, String) -> Unit,
    onToggleModule: (StudyPlan, Int) -> Unit,
    onPublishPlanToWorkshop: (StudyPlan) -> Unit,
    onDeletePlan: (String) -> Unit,
    onLaunchPlayPurchase: (String) -> Unit,
    onRestorePlayPurchases: () -> Unit,
    onRunAutomatedAgent: () -> Unit,
    onDownloadWorkshopItem: (WorkshopItem) -> Unit,
    onLikeWorkshopItem: (String) -> Unit,
    onPublishWorkshopItem: (String, String, String, String, List<String>) -> Unit
) {
    var selectedSubTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TabRow(selectedTabIndex = selectedSubTab) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Planificador", maxLines = 1) },
                modifier = Modifier.testTag("planner_tab_plans")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Tienda Play", maxLines = 1) },
                modifier = Modifier.testTag("planner_tab_pricing")
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Workshop", maxLines = 1) },
                modifier = Modifier.testTag("planner_tab_workshop")
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
        ) {
            when (selectedSubTab) {
                0 -> StudyPlansBuilderTab(
                    currentLevel = profile?.academicLevel ?: "Secundaria",
                    subjects = subjects,
                    plans = plans,
                    isGeneratingPlan = isGeneratingPlan,
                    onSaveSubject = onSaveSubject,
                    onGeneratePlan = onGeneratePlan,
                    onToggleModule = onToggleModule,
                    onPublishPlanToWorkshop = onPublishPlanToWorkshop,
                    onDeletePlan = onDeletePlan
                )
                1 -> MembershipPlansTab(
                    profile = profile,
                    playOffers = playOffers,
                    isPlayStoreReady = isPlayStoreReady,
                    automatedAgentState = automatedAgentState,
                    onLaunchPlayPurchase = onLaunchPlayPurchase,
                    onRestorePlayPurchases = onRestorePlayPurchases,
                    onRunAutomatedAgent = onRunAutomatedAgent
                )
                2 -> WorkshopScreen(
                    items = workshopItems,
                    onDownloadItem = onDownloadWorkshopItem,
                    onLikeItem = onLikeWorkshopItem,
                    onPublishItem = onPublishWorkshopItem
                )
            }
        }
    }
}

@Composable
private fun StudyPlansBuilderTab(
    currentLevel: String,
    subjects: List<SubjectRecord>,
    plans: List<StudyPlan>,
    isGeneratingPlan: Boolean,
    onSaveSubject: (String, Double, Long) -> Unit,
    onGeneratePlan: (String, Int, String) -> Unit,
    onToggleModule: (StudyPlan, Int) -> Unit,
    onPublishPlanToWorkshop: (StudyPlan) -> Unit,
    onDeletePlan: (String) -> Unit
) {
    val levels = listOf("Primaria", "Secundaria", "Preparatoria", "Universidad", "Posgrado")
    val goals = listOf(
        "Reforzar materias bajas",
        "Preparar exámenes avanzados",
        "Equilibrio diario",
        "Alto rendimiento"
    )

    var selectedLevel by remember(currentLevel) { mutableStateOf(currentLevel) }
    var selectedGoal by remember { mutableStateOf(goals.first()) }
    var dailyMinutes by remember { mutableFloatStateOf(60f) }
    var showAddSubject by remember { mutableStateOf(false) }

    val weakNames = subjects.filter { it.priority == "reforzar" }.map { it.name }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Diseña tu Plan Estudiantil con IA",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Desde cero según tu nivel, materias bajas y tiempo diario.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { showAddSubject = true }) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Materia")
                        }
                    }

                    Text("Nivel académico:", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(levels) { lvl ->
                            FilterChip(
                                selected = selectedLevel == lvl,
                                onClick = { selectedLevel = lvl },
                                label = { Text(lvl) }
                            )
                        }
                    }

                    Text("Objetivo principal:", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(goals) { goal ->
                            FilterChip(
                                selected = selectedGoal == goal,
                                onClick = { selectedGoal = goal },
                                label = { Text(goal) }
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Tiempo diario de estudio: ${dailyMinutes.toInt()} min",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Slider(
                            value = dailyMinutes,
                            onValueChange = { dailyMinutes = it },
                            valueRange = 30f..180f,
                            steps = 4
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (weakNames.isNotEmpty()) {
                                "La IA priorizará reforzar: ${weakNames.joinToString(", ")}"
                            } else if (subjects.isNotEmpty()) {
                                "Materias incluidas: ${subjects.joinToString { it.name }}"
                            } else {
                                "Sin materias registradas aún: se creará un plan base equilibrado."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Button(
                        onClick = {
                            onGeneratePlan(selectedLevel, dailyMinutes.toInt(), selectedGoal)
                        },
                        enabled = !isGeneratingPlan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_ai_plan_button")
                    ) {
                        if (isGeneratingPlan) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Creando plan personalizado...")
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Crear Plan con IA al minuto")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Mis Planes (${plans.size})",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (plans.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Todavía no tienes planes guardados.",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Genera uno arriba con IA o descarga un plan de la comunidad Workshop.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(plans, key = { it.id }) { plan ->
                StudyPlanCard(
                    plan = plan,
                    onToggleModule = { idx -> onToggleModule(plan, idx) },
                    onPublish = { onPublishPlanToWorkshop(plan) },
                    onDelete = { onDeletePlan(plan.id) }
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }

    if (showAddSubject) {
        AddSubjectDialog(
            onDismiss = { showAddSubject = false },
            onSave = { name, grade, hours ->
                onSaveSubject(name, grade, hours)
                showAddSubject = false
            }
        )
    }
}

@Composable
private fun MembershipPlansTab(
    profile: StudentProfile?,
    playOffers: List<PlayProductOfferUi>,
    isPlayStoreReady: Boolean,
    automatedAgentState: AiCallState<String>,
    onLaunchPlayPurchase: (String) -> Unit,
    onRestorePlayPurchases: () -> Unit,
    onRunAutomatedAgent: () -> Unit
) {
    val hasExtra25 = profile?.hasProtonExtra25 == true
    val hasLifetime = profile?.hasLifetimePass == true
    val hasMonthlyAgent = profile?.hasMonthlyAgentSub == true

    val extra25Offer = playOffers.firstOrNull { it.productId == PlayBillingManager.SKU_EXTRA_25 }
    val lifetimeOffer = playOffers.firstOrNull { it.productId == PlayBillingManager.SKU_LIFETIME_250 }
    val monthlyOffer = playOffers.firstOrNull { it.productId == PlayBillingManager.SKU_MONTHLY_AGENT }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Google Play Store · Planes Protón 2", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "Cobros reales procesados de forma segura mediante Google Play Billing. Tras exportar y subir el AAB a Google Play Console con los SKUs configurados, los cobros en Play Store se activan al instante.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isPlayStoreReady) StatusSuccess.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isPlayStoreReady) "Play Billing conectado" else "Play Store esperando publicación",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isPlayStoreReady) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    TextButton(
                        onClick = onRestorePlayPurchases,
                        modifier = Modifier.testTag("restore_play_purchases_button")
                    ) {
                        Icon(Icons.Filled.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Restaurar Compras")
                    }
                }
            }
        }

        // 1. Free Daily Plan
        item {
            PricingPlanCard(
                title = "Plan Estudiantil Gratuito",
                price = "$0 MXN / siempre",
                badge = if (!hasExtra25 && !hasLifetime && !hasMonthlyAgent) "ACTIVO" else "BASE",
                highlight = false,
                features = listOf(
                    "15 tickets de IA gratuitos renovados cada día",
                    "Calculadora científica, Tutora IA y Exámenes",
                    "Mascota Autosauro Base, Duelos IA y Clima Estacional"
                ),
                buttonLabel = "Plan Incluido (${profile?.ticketsRemaining ?: 15} tickets hoy)",
                buttonEnabled = false,
                onClick = {}
            )
        }

        // 2. Protón 2 Extra Tier ($25 MXN)
        item {
            val price25 = extra25Offer?.livePlayPrice ?: extra25Offer?.fallbackPriceLabel ?: "$25 MXN · Pago Único Extra"
            PricingPlanCard(
                title = "Acceso Extra Protón 2",
                price = price25,
                badge = if (hasExtra25) "ADQUIRIDO" else "EXTRA $25",
                highlight = hasExtra25,
                features = listOf(
                    "Sube a 40 tickets de IA diarios renovables",
                    "Desbloquea modo VIP para tu Mascota Autosauro Rex",
                    "Prioridad en conectores externos (Gemini / Cloud Code)"
                ),
                buttonLabel = if (hasExtra25) "Acceso Extra $25 Activo" else "Comprar con Google Play ($25 MXN)",
                buttonEnabled = !hasExtra25,
                testTag = "buy_extra_25_button",
                onClick = { onLaunchPlayPurchase(PlayBillingManager.SKU_EXTRA_25) }
            )
        }

        // 3. One-Time Lifetime Pass $250 MXN
        item {
            val price250 = lifetimeOffer?.livePlayPrice ?: lifetimeOffer?.fallbackPriceLabel ?: "$250 MXN · Pago Único"
            PricingPlanCard(
                title = "Pase Único Vitalicio",
                price = price250,
                badge = if (hasLifetime) "ADQUIRIDO" else "SIN MENSUALIDADES",
                highlight = hasLifetime,
                features = listOf(
                    "Tickets de IA completamente ilimitados de por vida",
                    "Acceso completo a Workshop, Diapositivas y Duelos IA",
                    "Un solo pago en Google Play sin cargos recurrentes"
                ),
                buttonLabel = if (hasLifetime) "Pase Único Vitalicio Activo" else "Comprar en Google Play ($250 MXN)",
                buttonEnabled = !hasLifetime,
                testTag = "activate_lifetime_pass_button",
                onClick = { onLaunchPlayPurchase(PlayBillingManager.SKU_LIFETIME_250) }
            )
        }

        // 4. Monthly Subscription Automated Premium Agent
        item {
            val priceMonthly = monthlyOffer?.livePlayPrice ?: monthlyOffer?.fallbackPriceLabel ?: "$149 MXN / mes"
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Agente Premium Automatizado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("$priceMonthly · Google Play", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (hasMonthlyAgent) StatusSuccess else MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = if (hasMonthlyAgent) "ACTIVO" else "SUSCRIPCIÓN",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    listOf(
                        "Analiza tus calificaciones + noticias de tu escuela en 1 toque",
                        "Crea automáticamente tu Plan Semanal y Tareas con procedimiento",
                        "Detecta profesores y redacta cómo ayudarles en su materia",
                        "Tickets de IA ilimitados y prioridad en todos los conectores"
                    ).forEach { feat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text(feat, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Button(
                        onClick = { onLaunchPlayPurchase(PlayBillingManager.SKU_MONTHLY_AGENT) },
                        enabled = !hasMonthlyAgent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_monthly_agent_button")
                    ) {
                        Icon(
                            imageVector = if (hasMonthlyAgent) Icons.Filled.AllInclusive else Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (hasMonthlyAgent) {
                                "Suscripción Mensual Activa (Gestionar en Play Store)"
                            } else {
                                "Suscribirse vía Google Play ($149/mes)"
                            }
                        )
                    }

                    if (hasMonthlyAgent) {
                        FilledTonalButton(
                            onClick = onRunAutomatedAgent,
                            enabled = automatedAgentState !is AiCallState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("run_automated_agent_button")
                        ) {
                            if (automatedAgentState is AiCallState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Agente ejecutando automatización total...")
                            } else {
                                Icon(Icons.Filled.Bolt, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Ejecutar Agente Automatizado Ahora")
                            }
                        }
                    }

                    when (automatedAgentState) {
                        is AiCallState.Success -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Reporte del Agente Premium:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Text(automatedAgentState.data, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        is AiCallState.Error -> {
                            Text(automatedAgentState.message, color = MaterialTheme.colorScheme.error)
                        }
                        else -> Unit
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun PricingPlanCard(
    title: String,
    price: String,
    badge: String,
    highlight: Boolean,
    features: List<String>,
    buttonLabel: String,
    buttonEnabled: Boolean,
    testTag: String? = null,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(price, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            features.forEach { feat ->
                Text("• $feat", style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedButton(
                onClick = onClick,
                enabled = buttonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .let { if (testTag != null) it.testTag(testTag) else it }
            ) {
                Text(buttonLabel)
            }
        }
    }
}

@Composable
private fun StudyPlanCard(
    plan: StudyPlan,
    onToggleModule: (Int) -> Unit,
    onPublish: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(plan.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${plan.academicLevel} · ${plan.dailyMinutes} min/día · Por ${plan.sourceAuthor}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Eliminar plan")
                }
            }

            Text(plan.summary, style = MaterialTheme.typography.bodyMedium)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { (plan.progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = "${plan.progressPercent}%",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            plan.modules.forEachIndexed { idx, mod ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        IconButton(
                            onClick = { onToggleModule(idx) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (mod.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = "Completar módulo",
                                tint = if (mod.completed) StatusSuccess else MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${mod.title} (${mod.durationMinutes} min)",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = mod.subject,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            mod.activities.forEach { act ->
                                Text(
                                    text = "• $act",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onPublish,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Compartir en Workshop")
            }
        }
    }
}
