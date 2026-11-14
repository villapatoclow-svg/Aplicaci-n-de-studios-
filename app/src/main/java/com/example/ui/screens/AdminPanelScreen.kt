package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminUserInspectBundle
import com.example.data.model.StudentProfile
import com.example.data.model.WorkshopItem
import com.example.data.repository.StudyRepository
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.AiCallState

@Composable
fun AdminPanelScreen(
    isAdmin: Boolean,
    allUsers: List<StudentProfile>,
    workshopItems: List<WorkshopItem>,
    inspectedUserState: AiCallState<AdminUserInspectBundle>,
    onInspectUser: (String) -> Unit,
    onGrantEntitlement: (userId: String, field: String, value: Any) -> Unit,
    onDeleteUserSubItem: (userId: String, subcollection: String, docId: String) -> Unit,
    onDeleteUser: (String) -> Unit,
    onDeleteWorkshopItem: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    if (!isAdmin) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Card(modifier = Modifier.widthIn(max = 420.dp)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Acceso Restringido", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "El Modo Administrador Protón 2 es exclusivo para la cuenta verificada ${StudyRepository.ADMIN_EMAIL}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text("Volver")
                    }
                }
            }
        }
        return
    }

    var searchQuery by remember { mutableStateOf("") }
    var userToDelete by remember { mutableStateOf<StudentProfile?>(null) }

    val filteredUsers = remember(allUsers, searchQuery) {
        if (searchQuery.isBlank()) allUsers
        else allUsers.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
                it.userId.contains(searchQuery, ignoreCase = true) ||
                it.schoolName.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Modo Administrador Exclusivo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Sesión verificada: ${StudyRepository.ADMIN_EMAIL}",
                            style = MaterialTheme.typography.labelMedium,
                            color = StatusSuccess
                        )
                    }
                }
            }

            // KPI Summary
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("${allUsers.size}", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMonoFontFamily), fontWeight = FontWeight.Bold)
                            Text("Usuarios registrados", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("${workshopItems.size}", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMonoFontFamily), fontWeight = FontWeight.Bold)
                            Text("Items en Workshop", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Search users
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    label = { Text("Buscar usuario por nombre, UID o escuela") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_search_input")
                )
            }

            // Inspected user detail card
            item {
                when (inspectedUserState) {
                    is AiCallState.Loading -> Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text("Cargando datos del usuario...")
                        }
                    }
                    is AiCallState.Error -> Text(inspectedUserState.message, color = MaterialTheme.colorScheme.error)
                    is AiCallState.Success -> {
                        val bundle = inspectedUserState.data
                        val u = bundle.profile
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Inspección: ${u.displayName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("UID: ${u.userId}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily))
                                        Text("${u.academicLevel} · Edad ${u.ageYears} · Mascota ${u.petName} (${u.petSpecies} Nv.${u.petLevel})", style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(onClick = { userToDelete = u }) {
                                        Icon(Icons.Filled.DeleteForever, contentDescription = "Eliminar usuario completo", tint = StatusDanger)
                                    }
                                }

                                Text("Permisos y pases:", style = MaterialTheme.typography.labelLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = u.hasProtonExtra25,
                                        onClick = { onGrantEntitlement(u.userId, "hasProtonExtra25", !u.hasProtonExtra25) },
                                        label = { Text("Extra $25") }
                                    )
                                    FilterChip(
                                        selected = u.hasLifetimePass,
                                        onClick = { onGrantEntitlement(u.userId, "hasLifetimePass", !u.hasLifetimePass) },
                                        label = { Text("Pase $250") }
                                    )
                                    FilterChip(
                                        selected = u.hasMonthlyAgentSub,
                                        onClick = { onGrantEntitlement(u.userId, "hasMonthlyAgentSub", !u.hasMonthlyAgentSub) },
                                        label = { Text("Agente Pro") }
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onGrantEntitlement(u.userId, "ticketsRemaining", u.ticketsRemaining + 25L) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Dar +25 Tickets IA (Actual: ${u.ticketsRemaining})")
                                }

                                Text(
                                    "Registros: ${bundle.tasks.size} tareas · ${bundle.plans.size} planes · ${bundle.subjects.size} materias · ${bundle.finances.size} finanzas",
                                    style = MaterialTheme.typography.labelLarge
                                )

                                bundle.tasks.take(6).forEach { task ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Tarea: ${task.title}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = { onDeleteUserSubItem(u.userId, "smart_tasks", task.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Borrar tarea", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                bundle.plans.take(4).forEach { plan ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Plan: ${plan.title}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = { onDeleteUserSubItem(u.userId, "study_plans", plan.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Borrar plan", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> Unit
                }
            }

            item {
                Text("Directorio de Usuarios (${filteredUsers.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(filteredUsers, key = { it.userId }) { user ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(user.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "${user.academicLevel} · ${user.petSpecies} Nv.${user.petLevel} · ${user.ticketsRemaining} tickets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = user.userId,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            OutlinedButton(
                                onClick = { onInspectUser(user.userId) },
                                modifier = Modifier.testTag("admin_inspect_${user.userId}")
                            ) {
                                Icon(Icons.Filled.PersonSearch, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Checar")
                            }
                            IconButton(onClick = { userToDelete = user }) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Eliminar usuario", tint = StatusDanger)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text("Moderación Workshop (${workshopItems.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(workshopItems, key = { it.id }) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Por ${item.authorName} · ${item.category} · ${item.downloadsCount} descargas", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(
                            onClick = { onDeleteWorkshopItem(item.id) },
                            modifier = Modifier.testTag("admin_delete_workshop_${item.id}")
                        ) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Borrar publicación", tint = StatusDanger)
                        }
                    }
                }
            }
        }
    }

    val target = userToDelete
    if (target != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Eliminar cuenta y registros") },
            text = { Text("¿Seguro que deseas borrar el perfil y todas las colecciones de '${target.displayName}' (${target.userId})?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger),
                    onClick = {
                        onDeleteUser(target.userId)
                        userToDelete = null
                    }
                ) {
                    Text("Eliminar todo")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}
