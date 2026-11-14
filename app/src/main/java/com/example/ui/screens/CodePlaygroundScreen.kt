package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.CodePlaygroundResult
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.viewmodel.AiCallState

@Composable
fun CodePlaygroundTab(
    codeState: AiCallState<CodePlaygroundResult>,
    onGenerateCode: (prompt: String, kind: String) -> Unit
) {
    var selectedKind by remember { mutableStateOf("game") }
    var prompt by remember { mutableStateOf("Juego espacial arcade donde esquivo asteroides con botones táctiles") }
    var viewMode by remember { mutableIntStateOf(0) } // 0 = Jugar / Probar en Vivo, 1 = Ver Código
    var reloadNonce by remember { mutableIntStateOf(0) }

    val quickIdeas = if (selectedKind == "game") {
        listOf(
            "Juego arcade de nave espacial con disparos y botones táctiles",
            "Snake neón con botones direccionales en pantalla",
            "Juego de atrapar estrellas con física rápida"
        )
    } else {
        listOf(
            "App Temporizador Pomodoro con ciclos y registro",
            "App Calculadora de Presupuesto mensual con barras",
            "App Pizarra de Notas Rápidas y checklist interactivo"
        )
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Código IA: Crea Juegos y Apps al Instante",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Despierta tu creatividad: la IA programa un videojuego jugable o una app interactiva y la ejecutas aquí mismo dentro de MentePro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedKind == "game",
                            onClick = {
                                selectedKind = "game"
                                prompt = "Juego arcade de nave espacial con botones táctiles"
                            },
                            leadingIcon = { Icon(Icons.Filled.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Crear Videojuego") }
                        )
                        FilterChip(
                            selected = selectedKind == "app",
                            onClick = {
                                selectedKind = "app"
                                prompt = "App Temporizador Pomodoro interactivo"
                            },
                            leadingIcon = { Icon(Icons.Filled.Apps, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Crear Aplicación") }
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickIdeas) { idea ->
                            FilterChip(
                                selected = prompt == idea,
                                onClick = { prompt = idea },
                                label = { Text(idea, maxLines = 1) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        label = {
                            Text(if (selectedKind == "game") "Describe tu videojuego" else "Describe tu aplicación")
                        },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("code_prompt_input")
                    )

                    Button(
                        onClick = {
                            viewMode = 0
                            onGenerateCode(prompt, selectedKind)
                        },
                        enabled = codeState !is AiCallState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_playable_code_button")
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (selectedKind == "game") "Crear y Jugar Ahora" else "Crear y Probar App Ahora"
                        )
                    }
                }
            }
        }

        item {
            when (codeState) {
                is AiCallState.Idle -> Unit
                is AiCallState.Loading -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Text("Programando HTML5, lógica y controles táctiles...")
                        }
                    }
                }
                is AiCallState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(codeState.message, modifier = Modifier.padding(16.dp))
                    }
                }
                is AiCallState.Success -> {
                    val res = codeState.data
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(res.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(res.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { reloadNonce += 1 }) {
                                    Icon(Icons.Filled.Refresh, contentDescription = "Reiniciar vista en vivo")
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = viewMode == 0,
                                    onClick = { viewMode = 0 },
                                    leadingIcon = { Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    label = { Text("Probar en Vivo") }
                                )
                                FilterChip(
                                    selected = viewMode == 1,
                                    onClick = { viewMode = 1 },
                                    leadingIcon = { Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    label = { Text("Código Fuente") }
                                )
                            }

                            if (viewMode == 0) {
                                SandboxedPlayableWebView(
                                    html = res.htmlPreview,
                                    reloadKey = "${res.title}_$reloadNonce",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(420.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                                Text(
                                    text = "Instrucciones: ${res.instructions}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = res.codeSnippet.ifBlank { res.htmlPreview.take(1800) },
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                        modifier = Modifier
                                            .horizontalScroll(rememberScrollState())
                                            .padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SandboxedPlayableWebView(
    html: String,
    reloadKey: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = false
                    allowFileAccess = false
                    allowContentAccess = false
                    builtInZoomControls = false
                    displayZoomControls = false
                }
                setTag(reloadKey)
                loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            if (webView.tag != reloadKey) {
                webView.tag = reloadKey
                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier
    )
}
