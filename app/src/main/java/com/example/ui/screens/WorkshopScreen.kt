package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.WorkshopItem

@Composable
fun WorkshopScreen(
    items: List<WorkshopItem>,
    onDownloadItem: (WorkshopItem) -> Unit,
    onLikeItem: (String) -> Unit,
    onPublishItem: (title: String, category: String, subject: String, description: String, steps: List<String>) -> Unit
) {
    val categories = listOf("todos" to "Todos", "plan" to "Planes", "actividad" to "Actividades", "examen" to "Exámenes", "guia" to "Guías")
    var selectedCategory by remember { mutableStateOf("todos") }
    var searchQuery by remember { mutableStateOf("") }
    var showPublishDialog by remember { mutableStateOf(false) }

    val filtered = remember(items, selectedCategory, searchQuery) {
        items.filter { item ->
            val catOk = selectedCategory == "todos" || item.category == selectedCategory
            val qOk = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.subject.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
            catOk && qOk
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
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.height(140.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_workshop_banner),
                            contentDescription = "Comunidad Workshop",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Workshop Comunitaria", style = MaterialTheme.typography.headlineSmall)
                                Text(
                                    "Descarga planes, actividades y guías creadas por otros estudiantes.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { showPublishDialog = true },
                                modifier = Modifier.testTag("publish_workshop_button")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Publicar")
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    label = { Text("Buscar por materia o título...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { (key, label) ->
                        FilterChip(
                            selected = selectedCategory == key,
                            onClick = { selectedCategory = key },
                            label = { Text(label) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("La Workshop está lista para tu aporte", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Sé el primero en publicar un plan estudiantil o actividad para ayudar a la comunidad.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { item ->
                    WorkshopItemCard(
                        item = item,
                        onDownload = { onDownloadItem(item) },
                        onLike = { onLikeItem(item.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }

    if (showPublishDialog) {
        PublishWorkshopDialog(
            onDismiss = { showPublishDialog = false },
            onPublish = { title, cat, subj, desc, steps ->
                onPublishItem(title, cat, subj, desc, steps)
                showPublishDialog = false
            }
        )
    }
}

@Composable
private fun WorkshopItemCard(
    item: WorkshopItem,
    onDownload: () -> Unit,
    onLike: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${item.category.uppercase()} · ${item.subject}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = item.academicLevel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Por ${item.authorName}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(item.description, style = MaterialTheme.typography.bodyMedium)

            if (item.steps.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    item.steps.take(3).forEachIndexed { idx, s ->
                        Text("${idx + 1}. $s", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLike) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "Dar me gusta",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text("${item.likesCount}", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(12.dp))
                    Text("${item.downloadsCount} descargas", style = MaterialTheme.typography.labelMedium)
                }

                Button(onClick = onDownload) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Descargar")
                }
            }
        }
    }
}

@Composable
private fun PublishWorkshopDialog(
    onDismiss: () -> Unit,
    onPublish: (String, String, String, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("plan") }
    var subject by remember { mutableStateOf("Matemáticas") }
    var description by remember { mutableStateOf("") }
    var stepsRaw by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publicar en Workshop") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("plan", "actividad", "examen", "guia")) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stepsRaw,
                    onValueChange = { stepsRaw = it },
                    label = { Text("Módulos o pasos (uno por línea)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val steps = stepsRaw.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    onPublish(title, category, subject, description, steps)
                },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Publicar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
