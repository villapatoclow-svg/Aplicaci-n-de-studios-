package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ExecutiveAdvisorResult
import com.example.data.model.FinanceEntry
import com.example.data.model.RoutePlanResult
import com.example.data.model.StudentProfile
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.AiCallState
import java.util.Locale

@Composable
fun PlanAltoScreen(
    profile: StudentProfile?,
    financeEntries: List<FinanceEntry>,
    routePlanState: AiCallState<RoutePlanResult>,
    executiveAdvisorState: AiCallState<ExecutiveAdvisorResult>,
    onUpdateHigherEdProfile: (ageYears: Long, level: String, degreeOrMaster: String) -> Unit,
    onAddFinanceEntry: (title: String, amount: Double, entryType: String, category: String, note: String) -> Unit,
    onDeleteFinanceEntry: (String) -> Unit,
    onPlanRoute: (origin: String, destination: String, mode: String) -> Unit,
    onRunExecutiveAdvisor: (query: String, autoCreateTasks: Boolean) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var showProfileEditor by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Serious Executive Banner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.BusinessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Plan Alto · Prepa y 18+",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (profile?.isAdultOrHigherEd == true) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    text = "${profile?.ageYears ?: 18} años",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = profile?.degreeOrMaster?.takeIf { it.isNotBlank() }
                                ?: "Configura tu carrera, maestría o preparatoria",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                OutlinedButton(
                    onClick = { showProfileEditor = true },
                    modifier = Modifier.testTag("edit_higher_ed_button")
                ) {
                    Text("Carrera / Edad")
                }
            }
        }

        TabRow(selectedTabIndex = selectedSubTab) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Finanzas y Bienes", maxLines = 1) },
                modifier = Modifier.testTag("plan_alto_tab_finances")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Rutas Campus", maxLines = 1) },
                modifier = Modifier.testTag("plan_alto_tab_routes")
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Asistente 18+", maxLines = 1) },
                modifier = Modifier.testTag("plan_alto_tab_advisor")
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
        ) {
            when (selectedSubTab) {
                0 -> FinancesAndAssetsTab(
                    entries = financeEntries,
                    onAddEntry = onAddFinanceEntry,
                    onDeleteEntry = onDeleteFinanceEntry
                )
                1 -> UniversityRoutesTab(
                    degreeOrMaster = profile?.degreeOrMaster.orEmpty(),
                    routePlanState = routePlanState,
                    onPlanRoute = onPlanRoute
                )
                2 -> ExecutiveAdvisorTab(
                    profile = profile,
                    executiveAdvisorState = executiveAdvisorState,
                    onRunAdvisor = onRunExecutiveAdvisor
                )
            }
        }
    }

    if (showProfileEditor) {
        HigherEdProfileDialog(
            currentAge = profile?.ageYears ?: 18L,
            currentLevel = profile?.academicLevel ?: "Preparatoria",
            currentDegree = profile?.degreeOrMaster.orEmpty(),
            onDismiss = { showProfileEditor = false },
            onSave = { age, lvl, deg ->
                onUpdateHigherEdProfile(age, lvl, deg)
                showProfileEditor = false
            }
        )
    }
}

@Composable
private fun FinancesAndAssetsTab(
    entries: List<FinanceEntry>,
    onAddEntry: (String, Double, String, String, String) -> Unit,
    onDeleteEntry: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("todos") }

    val totalIncome = remember(entries) { entries.filter { it.entryType == "ingreso" }.sumOf { it.amount } }
    val totalExpense = remember(entries) { entries.filter { it.entryType == "gasto" }.sumOf { it.amount } }
    val totalAssets = remember(entries) { entries.filter { it.entryType == "bien" }.sumOf { it.amount } }
    val netAvailable = totalIncome - totalExpense

    val filtered = remember(entries, filterType) {
        if (filterType == "todos") entries else entries.filter { it.entryType == filterType }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Control de Dinero y Bienes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Balance líquido: $${String.format(Locale.US, "%,.2f", netAvailable)}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = if (netAvailable >= 0) StatusSuccess else StatusDanger
                            )
                        }
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.testTag("add_finance_entry_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Registrar")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinanceMetricTile(
                            label = "Ingresos",
                            value = "$${String.format(Locale.US, "%,.0f", totalIncome)}",
                            color = StatusSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        FinanceMetricTile(
                            label = "Gastos",
                            value = "$${String.format(Locale.US, "%,.0f", totalExpense)}",
                            color = StatusDanger,
                            modifier = Modifier.weight(1f)
                        )
                        FinanceMetricTile(
                            label = "Bienes",
                            value = "$${String.format(Locale.US, "%,.0f", totalAssets)}",
                            color = StatusInfo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val tabs = listOf("todos" to "Todos", "ingreso" to "Ingresos", "gasto" to "Gastos", "bien" to "Bienes")
                items(tabs) { (key, label) ->
                    FilterChip(
                        selected = filterType == key,
                        onClick = { filterType = key },
                        label = { Text(label) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(6.dp))
                        Text("Registra tus becas, ingresos, gastos o bienes (laptop, equipo, ahorros).")
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { item ->
                val (badgeLabel, badgeColor, icon) = when (item.entryType) {
                    "ingreso" -> Triple("Ingreso", StatusSuccess, Icons.AutoMirrored.Filled.TrendingUp)
                    "bien" -> Triple("Bien", StatusInfo, Icons.Filled.VerifiedUser)
                    else -> Triple("Gasto", StatusDanger, Icons.AutoMirrored.Filled.TrendingDown)
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = badgeColor)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$badgeLabel · ${item.category}${if (item.note.isNotBlank()) " · ${item.note}" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$${String.format(Locale.US, "%,.0f", item.amount)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = badgeColor
                        )
                        IconButton(onClick = { onDeleteEntry(item.id) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Eliminar movimiento")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFinanceEntryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { t, amt, type, cat, note ->
                onAddEntry(t, amt, type, cat, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun FinanceMetricTile(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.10f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun UniversityRoutesTab(
    degreeOrMaster: String,
    routePlanState: AiCallState<RoutePlanResult>,
    onPlanRoute: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var transportMode by remember { mutableStateOf("Transporte público") }
    val modes = listOf("Transporte público", "Auto", "Bicicleta", "Caminando")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Rutas a tu Universidad o Destino",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Aunque tu facultad, campus o maestría (${degreeOrMaster.ifBlank { "Estudios Superiores" }}) esté lejos, calcula la mejor ruta con calles, avenidas y transbordos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = origin,
                        onValueChange = { origin = it },
                        label = { Text("Origen (colonia, calle o estación)") },
                        placeholder = { Text("Ej. Metro Pantitlán / Col. Centro") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("route_origin_input")
                    )
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Universidad, Campus o Destino") },
                        placeholder = { Text("Ej. Ciudad Universitaria UNAM / Facultad de Ingeniería") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("route_destination_input")
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(modes) { mode ->
                            FilterChip(
                                selected = transportMode == mode,
                                onClick = { transportMode = mode },
                                label = { Text(mode) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onPlanRoute(origin, destination, transportMode) },
                            enabled = routePlanState !is AiCallState.Loading,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("plan_route_button")
                        ) {
                            Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Calcular Ruta IA")
                        }
                        if (origin.isNotBlank() && destination.isNotBlank()) {
                            FilledTonalButton(
                                onClick = {
                                    val url = "https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(origin)}&destination=${Uri.encode(destination)}"
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Mapa")
                            }
                        }
                    }
                }
            }
        }

        item {
            when (routePlanState) {
                is AiCallState.Idle -> Unit
                is AiCallState.Loading -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Text("Analizando avenidas, calles y transbordos...")
                        }
                    }
                }
                is AiCallState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(routePlanState.message, modifier = Modifier.padding(16.dp))
                    }
                }
                is AiCallState.Success -> {
                    val route = routePlanState.data
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tiempo: ${route.totalEstimatedTime}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Costo: ${route.estimatedCost}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily)
                                )
                            }

                            Text("Calles y Vialidades Clave:", style = MaterialTheme.typography.labelLarge)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(route.streetHighlights) { st ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = st,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Text("Itinerario Paso a Paso:", style = MaterialTheme.typography.labelLarge)
                            route.routeSteps.forEachIndexed { idx, step ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${idx + 1}. ${step.streetOrLine}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = step.distanceOrDuration,
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily)
                                            )
                                        }
                                        Text(step.instruction, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StatusSuccess.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Consejo: ${route.tips}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExecutiveAdvisorTab(
    profile: StudentProfile?,
    executiveAdvisorState: AiCallState<ExecutiveAdvisorResult>,
    onRunAdvisor: (String, Boolean) -> Unit
) {
    var decisionQuery by remember { mutableStateOf("") }
    var autoCreateTasks by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Asistente Personal Automatizado 18+",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Automatiza tu agenda ejecutiva, evalúa compras con base en tus finanzas reales (${profile?.academicLevel ?: "Superior"}) y te explica qué SÍ comprar y por qué NO comprar ciertos gastos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = decisionQuery,
                        onValueChange = { decisionQuery = it },
                        label = { Text("¿Estás pensando en comprar algo o tomar una decisión?") },
                        placeholder = { Text("Ej. Quiero comprar audífonos de $2,500 y libros del semestre") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("advisor_query_input")
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = autoCreateTasks, onCheckedChange = { autoCreateTasks = it })
                        Text("Convertir acciones recomendadas en Tareas Inteligentes", style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = { onRunAdvisor(decisionQuery, autoCreateTasks) },
                        enabled = executiveAdvisorState !is AiCallState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_executive_advisor_button")
                    ) {
                        Icon(Icons.Filled.BusinessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Automatizar Mi Día y Compras")
                    }
                }
            }
        }

        item {
            when (executiveAdvisorState) {
                is AiCallState.Idle -> Unit
                is AiCallState.Loading -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Text("Analizando presupuesto, prioridades y compras...")
                        }
                    }
                }
                is AiCallState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(executiveAdvisorState.message, modifier = Modifier.padding(16.dp))
                    }
                }
                is AiCallState.Success -> {
                    val res = executiveAdvisorState.data
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Plan Ejecutivo del Día", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(res.dailyPriorityPlan, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.ShoppingBag, contentDescription = null, tint = StatusSuccess)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Qué SÍ te conviene comprar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                                res.buyList.forEach { adv ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = StatusSuccess.copy(alpha = 0.10f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("${adv.item} (${adv.estimatedCost})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                            Text(adv.reason, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Block, contentDescription = null, tint = StatusDanger)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Qué NO debes comprar y por qué", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                                res.avoidBuyingList.forEach { adv ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = StatusWarning.copy(alpha = 0.14f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("${adv.item} (${adv.estimatedCost})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = StatusDanger)
                                            Text(adv.reason, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Acciones Automatizadas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                res.automationActions.forEach { act ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(act, style = MaterialTheme.typography.bodyMedium)
                                    }
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
private fun HigherEdProfileDialog(
    currentAge: Long,
    currentLevel: String,
    currentDegree: String,
    onDismiss: () -> Unit,
    onSave: (Long, String, String) -> Unit
) {
    var ageText by remember { mutableStateOf(currentAge.toString()) }
    var level by remember { mutableStateOf(if (currentLevel in listOf("Preparatoria", "Universidad", "Posgrado")) currentLevel else "Universidad") }
    var degree by remember { mutableStateOf(currentDegree) }
    val levels = listOf("Preparatoria", "Universidad", "Posgrado")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Perfil Plan Alto (18+ / Posgrado)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Tu edad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(levels) { lvl ->
                        FilterChip(
                            selected = level == lvl,
                            onClick = { level = lvl },
                            label = { Text(lvl) }
                        )
                    }
                }
                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Carrera, Maestría o Especialidad") },
                    placeholder = { Text("Ej. Maestría en Ciencia de Datos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAge = ageText.toLongOrNull() ?: 18L
                    onSave(parsedAge, level, degree)
                }
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
private fun AddFinanceEntryDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var entryType by remember { mutableStateOf("gasto") }
    var category by remember { mutableStateOf("Universidad") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Finanzas o Bienes") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ingreso" to "Ingreso", "gasto" to "Gasto", "bien" to "Bien").forEach { (k, lbl) ->
                        FilterChip(
                            selected = entryType == k,
                            onClick = { entryType = k },
                            label = { Text(lbl) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Concepto o Bien") },
                    placeholder = { Text("Ej. Beca CONAHCYT / Laptop / Renta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                    onSave(title, amt, entryType, category, note)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
