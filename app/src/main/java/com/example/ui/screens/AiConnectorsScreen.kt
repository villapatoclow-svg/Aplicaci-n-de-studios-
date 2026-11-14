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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.AiConnectorConfig
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.AiCallState

@Composable
fun AiConnectorsScreen(
    config: AiConnectorConfig,
    testState: AiCallState<String>,
    onSaveConfig: (AiConnectorConfig) -> Unit,
    onTestConnector: (AiConnectorConfig) -> Unit,
    onClearKeys: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var draft by remember(config) { mutableStateOf(config) }
    var showKeys by remember { mutableStateOf(false) }

    val providers = listOf(
        "firebase_ai" to "Firebase AI (Incluido)",
        "gemini_byok" to "Gemini API Key",
        "cloud_code" to "Cloud Code / Claude",
        "openai_compatible" to "Otro Servicio API"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("connectors_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text("Conectores IA y Claves API", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Motor activo: ${draft.activeLabel()}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { showKeys = !showKeys }) {
                        Icon(
                            imageVector = if (showKeys) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showKeys) "Ocultar claves" else "Mostrar claves"
                        )
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Tus claves personales se cifran en tu dispositivo con Android Keystore (AES-GCM) y nunca se suben a la nube ni a Firebase. Todos los agentes (Tutora, Calculadora, Planes y Código) usarán el conector elegido.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
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
                        Text("Selecciona el Proveedor para tus Agentes:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(providers) { (id, label) ->
                                FilterChip(
                                    selected = draft.activeProvider == id,
                                    onClick = { draft = draft.copy(activeProvider = id) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            // Gemini BYOK Card
            item {
                val transform = if (showKeys) VisualTransformation.None else PasswordVisualTransformation()
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Gemini API Directa (Google AI)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = draft.geminiApiKey,
                            onValueChange = { draft = draft.copy(geminiApiKey = it) },
                            label = { Text("Gemini API Key (AIza...)") },
                            visualTransformation = transform,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("gemini_key_input")
                        )
                        OutlinedTextField(
                            value = draft.geminiModel,
                            onValueChange = { draft = draft.copy(geminiModel = it) },
                            label = { Text("Modelo Gemini (ej. gemini-2.5-flash)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Cloud Code / Anthropic Claude Card
            item {
                val transform = if (showKeys) VisualTransformation.None else PasswordVisualTransformation()
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text("Conector Cloud Code / Anthropic", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = draft.cloudCodeApiKey,
                            onValueChange = { draft = draft.copy(cloudCodeApiKey = it) },
                            label = { Text("Cloud Code / Anthropic API Key (sk-ant-...)") },
                            visualTransformation = transform,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("cloud_code_key_input")
                        )
                        OutlinedTextField(
                            value = draft.cloudCodeEndpoint,
                            onValueChange = { draft = draft.copy(cloudCodeEndpoint = it) },
                            label = { Text("Endpoint URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = draft.cloudCodeModel,
                            onValueChange = { draft = draft.copy(cloudCodeModel = it) },
                            label = { Text("Modelo (ej. claude-sonnet-4-20250514)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Custom OpenAI-compatible service Card
            item {
                val transform = if (showKeys) VisualTransformation.None else PasswordVisualTransformation()
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(Modifier.width(8.dp))
                            Text("Otros Servicios Compatibles (OpenAI / Groq / Custom)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = draft.customApiKey,
                            onValueChange = { draft = draft.copy(customApiKey = it) },
                            label = { Text("API Key externa (Bearer)") },
                            visualTransformation = transform,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("custom_key_input")
                        )
                        OutlinedTextField(
                            value = draft.customBaseUrl,
                            onValueChange = { draft = draft.copy(customBaseUrl = it) },
                            label = { Text("URL Chat Completions") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = draft.customModel,
                            onValueChange = { draft = draft.copy(customModel = it) },
                            label = { Text("Nombre del modelo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onSaveConfig(draft) },
                            modifier = Modifier.weight(1f).testTag("save_connectors_button")
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Guardar y Activar")
                        }
                        FilledTonalButton(
                            onClick = { onTestConnector(draft) },
                            enabled = testState !is AiCallState.Loading,
                            modifier = Modifier.weight(1f).testTag("test_connector_button")
                        ) {
                            if (testState is AiCallState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text("Probar Conexión")
                        }
                    }

                    when (testState) {
                        is AiCallState.Success -> Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StatusSuccess.copy(alpha = 0.14f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Conexión exitosa: ${testState.data}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StatusSuccess,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        is AiCallState.Error -> Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = testState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        else -> Unit
                    }

                    OutlinedButton(
                        onClick = {
                            onClearKeys()
                            draft = AiConnectorConfig()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Borrar claves y volver a Firebase AI")
                    }
                    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text("Volver al Dashboard")
                    }
                }
            }
        }
    }
}
