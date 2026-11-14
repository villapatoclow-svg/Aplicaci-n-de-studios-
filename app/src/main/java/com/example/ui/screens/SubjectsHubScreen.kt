package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ArtsGuideResult
import com.example.data.model.SchoolAutomationResult
import com.example.data.model.SchoolTeacher
import com.example.data.model.StudentProfile
import com.example.data.model.SubjectCatalogModule
import com.example.domain.SubjectCatalog
import com.example.ui.viewmodel.AiCallState

@Composable
fun SubjectsHubScreen(
    profile: StudentProfile?,
    teachers: List<SchoolTeacher>,
    schoolAutomationState: AiCallState<SchoolAutomationResult>,
    artsGuideState: AiCallState<ArtsGuideResult>,
    onAutomateSchool: (String, String) -> Unit,
    onAddManualTeacher: (String, String, String, String) -> Unit,
    onDeleteTeacher: (String) -> Unit,
    onGenerateArtsHelp: (String, String) -> Unit,
    onSaveArtsTask: (ArtsGuideResult) -> Unit,
    onCreateTopicTask: (String, String) -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenTutor: () -> Unit,
    onOpenDuels: () -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var selectedModule by remember { mutableStateOf<SubjectCatalogModule?>(null) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TabRow(selectedTabIndex = selectedSubTab) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = {
                    selectedSubTab = 0
                    selectedModule = null
                },
                text = { Text("Módulos y Artes", maxLines = 1) },
                modifier = Modifier.testTag("subjects_tab_modules")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = {
                    selectedSubTab = 1
                    selectedModule = null
                },
                text = { Text("Mi Secundaria", maxLines = 1) },
                modifier = Modifier.testTag("subjects_tab_school")
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
        ) {
            when (selectedSubTab) {
                0 -> {
                    val activeMod = selectedModule
                    if (activeMod != null) {
                        SubjectModuleDetailView(
                            module = activeMod,
                            artsGuideState = artsGuideState,
                            onBack = { selectedModule = null },
                            onGenerateArtsHelp = onGenerateArtsHelp,
                            onSaveArtsTask = onSaveArtsTask,
                            onCreateTopicTask = onCreateTopicTask,
                            onOpenCalculator = onOpenCalculator,
                            onOpenTutor = onOpenTutor,
                            onOpenDuels = onOpenDuels
                        )
                    } else {
                        SubjectCatalogListView(
                            initialLevel = profile?.academicLevel ?: "Secundaria",
                            artsGuideState = artsGuideState,
                            onSelectModule = { selectedModule = it },
                            onGenerateArtsHelp = onGenerateArtsHelp,
                            onSaveArtsTask = onSaveArtsTask
                        )
                    }
                }
                1 -> SchoolAndTeachersView(
                    profile = profile,
                    teachers = teachers,
                    schoolAutomationState = schoolAutomationState,
                    onAutomateSchool = onAutomateSchool,
                    onAddManualTeacher = onAddManualTeacher,
                    onDeleteTeacher = onDeleteTeacher
                )
            }
        }
    }
}

@Composable
private fun SubjectCatalogListView(
    initialLevel: String,
    artsGuideState: AiCallState<ArtsGuideResult>,
    onSelectModule: (SubjectCatalogModule) -> Unit,
    onGenerateArtsHelp: (String, String) -> Unit,
    onSaveArtsTask: (ArtsGuideResult) -> Unit
) {
    var selectedLevel by remember(initialLevel) {
        mutableStateOf(
            if (initialLevel in SubjectCatalog.academicLevels) initialLevel else "Secundaria"
        )
    }
    val filteredModules = remember(selectedLevel) {
        SubjectCatalog.modules.filter { it.level == selectedLevel }
    }
    var showArtsStudio by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Módulos por Materia y Nivel",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Selecciona tu nivel escolar desde Primaria hasta Carrera Universitaria para abrir el módulo de cada asignatura.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SubjectCatalog.academicLevels) { lvl ->
                        FilterChip(
                            selected = selectedLevel == lvl,
                            onClick = { selectedLevel = lvl },
                            label = { Text(lvl) },
                            modifier = Modifier.testTag("level_chip_$lvl")
                        )
                    }
                }
            }
        }

        // Quick Arts Studio Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Filled.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Estudio Creativo de Artes IA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Dibujo, pintura, música, teatro, literatura y diseño",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        FilledTonalButton(
                            onClick = { showArtsStudio = !showArtsStudio },
                            modifier = Modifier.testTag("toggle_arts_studio_button")
                        ) {
                            Text(if (showArtsStudio) "Ocultar" else "Abrir")
                        }
                    }

                    if (showArtsStudio) {
                        ArtsHelperPanel(
                            artsGuideState = artsGuideState,
                            onGenerateArtsHelp = onGenerateArtsHelp,
                            onSaveArtsTask = onSaveArtsTask
                        )
                    }
                }
            }
        }

        items(filteredModules, key = { it.id }) { mod ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subject_module_${mod.id}")
                    .clickable { onSelectModule(mod) }
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mod.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${mod.level} · ${mod.category}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Abrir módulo",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    Text(
                        text = mod.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(mod.keyTopics) { topic ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

@Composable
private fun SubjectModuleDetailView(
    module: SubjectCatalogModule,
    artsGuideState: AiCallState<ArtsGuideResult>,
    onBack: () -> Unit,
    onGenerateArtsHelp: (String, String) -> Unit,
    onSaveArtsTask: (ArtsGuideResult) -> Unit,
    onCreateTopicTask: (String, String) -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenTutor: () -> Unit,
    onOpenDuels: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a materias")
                }
                Column {
                    Text(module.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "${module.level} · ${module.category}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Descripción del Módulo", style = MaterialTheme.typography.titleMedium)
                    Text(module.description, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onOpenTutor, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Tutora IA")
                        }
                        FilledTonalButton(onClick = onOpenDuels, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Duelo IA")
                        }
                    }
                    if (module.specialToolType == "calculator") {
                        OutlinedButton(onClick = onOpenCalculator, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Abrir Calculadora Científica Paso a Paso")
                        }
                    }
                }
            }
        }

        item {
            Text("Temas Clave (toca para crear Tarea IA)", style = MaterialTheme.typography.titleMedium)
        }

        items(module.keyTopics) { topic ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = topic,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalButton(
                        onClick = {
                            onCreateTopicTask("Práctica paso a paso sobre $topic", module.name)
                        }
                    ) {
                        Icon(Icons.Filled.AddTask, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Crear Tarea")
                    }
                }
            }
        }

        if (module.specialToolType == "arts" || module.category == "Artes") {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Asistente Creativo de Artes", style = MaterialTheme.typography.titleMedium)
                        ArtsHelperPanel(
                            artsGuideState = artsGuideState,
                            onGenerateArtsHelp = onGenerateArtsHelp,
                            onSaveArtsTask = onSaveArtsTask
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtsHelperPanel(
    artsGuideState: AiCallState<ArtsGuideResult>,
    onGenerateArtsHelp: (String, String) -> Unit,
    onSaveArtsTask: (ArtsGuideResult) -> Unit
) {
    var selectedDiscipline by remember { mutableStateOf(SubjectCatalog.artsDisciplines.first()) }
    var topic by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(SubjectCatalog.artsDisciplines) { disc ->
                FilterChip(
                    selected = selectedDiscipline == disc,
                    onClick = { selectedDiscipline = disc },
                    label = { Text(disc) }
                )
            }
        }
        OutlinedTextField(
            value = topic,
            onValueChange = { topic = it },
            label = { Text("¿Qué proyecto o técnica de Artes estás haciendo?") },
            placeholder = { Text("Ej. Paisaje en acuarela con perspectiva, o canción rítmica escolar") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onGenerateArtsHelp(selectedDiscipline, topic) },
            enabled = topic.isNotBlank() && artsGuideState !is AiCallState.Loading,
            modifier = Modifier.fillMaxWidth().testTag("generate_arts_help_button")
        ) {
            Icon(Icons.Filled.Brush, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Generar Guía Creativa IA")
        }

        when (artsGuideState) {
            is AiCallState.Idle -> Unit
            is AiCallState.Loading -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("Diseñando paleta, materiales y pasos...")
                }
            }
            is AiCallState.Error -> Text(artsGuideState.message, color = MaterialTheme.colorScheme.error)
            is AiCallState.Success -> {
                val res = artsGuideState.data
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(res.projectTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Paleta / Atmósfera: ${res.paletteOrMood}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text("Materiales: ${res.materials.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                        res.steps.forEachIndexed { idx, step ->
                            Text("${idx + 1}. $step", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("Consejo maestro: ${res.masteryTip}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        FilledTonalButton(onClick = { onSaveArtsTask(res) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Guardar en Tareas Inteligentes")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SchoolAndTeachersView(
    profile: StudentProfile?,
    teachers: List<SchoolTeacher>,
    schoolAutomationState: AiCallState<SchoolAutomationResult>,
    onAutomateSchool: (String, String) -> Unit,
    onAddManualTeacher: (String, String, String, String) -> Unit,
    onDeleteTeacher: (String) -> Unit
) {
    var schoolName by remember(profile?.schoolName) {
        mutableStateOf(profile?.schoolName ?: "")
    }
    var schoolNews by remember(profile?.schoolNews) {
        mutableStateOf(profile?.schoolNews ?: "")
    }
    var showAddTeacherDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Mi Secundaria / Escuela", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "Automatiza tareas, profes y cómo apoyarles según las noticias escolares.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("Nombre de tu Secundaria o Escuela") },
                        placeholder = { Text("Ej. Secundaria Técnica #14") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = schoolNews,
                        onValueChange = { schoolNews = it },
                        label = { Text("Noticias, avisos o eventos de tu escuela") },
                        placeholder = { Text("Ej. Feria de ciencias el viernes, examen de Historia el lunes, campaña de reciclaje...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { onAutomateSchool(schoolName, schoolNews) },
                        enabled = schoolName.isNotBlank() && schoolNews.isNotBlank() && schoolAutomationState !is AiCallState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("automate_school_button")
                    ) {
                        if (schoolAutomationState is AiCallState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Automatizando escuela y profes...")
                        } else {
                            Icon(Icons.Filled.Newspaper, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Automatizar según Noticias con IA")
                        }
                    }

                    when (schoolAutomationState) {
                        is AiCallState.Success -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Resumen de Automatización:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Text(schoolAutomationState.data.actionSummary, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        is AiCallState.Error -> {
                            Text(schoolAutomationState.message, color = MaterialTheme.colorScheme.error)
                        }
                        else -> Unit
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Profesores y Cómo Ayudarles (${teachers.size})", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Estrategias prácticas para destacar y apoyar en cada materia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = { showAddTeacherDialog = true }) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Profe")
                }
            }
        }

        if (teachers.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Agrega las noticias de tu escuela arriba para que la IA detecte a tus profesores y cómo apoyar en clase, o añade uno manualmente.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(teachers, key = { it.id }) { teacher ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(teacher.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = teacher.subject,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { onDeleteTeacher(teacher.id) }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Eliminar profesor")
                            }
                        }
                        if (teacher.focusNote.isNotBlank()) {
                            Text(
                                text = "Enfoque actual: ${teacher.focusNote}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Filled.VolunteerActivism,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Cómo ayudar al profe en esta materia:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(teacher.howToHelp, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }

    if (showAddTeacherDialog) {
        AddTeacherDialog(
            onDismiss = { showAddTeacherDialog = false },
            onSave = { name, subj, focus, help ->
                onAddManualTeacher(name, subj, focus, help)
                showAddTeacherDialog = false
            }
        )
    }
}

@Composable
private fun AddTeacherDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var focusNote by remember { mutableStateOf("") }
    var howToHelp by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Profesor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del profe") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Materia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = focusNote,
                    onValueChange = { focusNote = it },
                    label = { Text("Tema o aviso próximo") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = howToHelp,
                    onValueChange = { howToHelp = it },
                    label = { Text("¿Cómo puedes ayudarle?") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, subject, focusNote, howToHelp) },
                enabled = name.isNotBlank() && subject.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
