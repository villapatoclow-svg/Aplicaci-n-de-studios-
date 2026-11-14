package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.PlayBillingManager
import com.example.core.SeasonalThemeManager
import com.example.data.model.StudentProfile

@Composable
fun ProfileSettingsDialog(
    profile: StudentProfile?,
    isVisitorMode: Boolean,
    isAdmin: Boolean,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onSetSessionMode: (String) -> Unit,
    onSetThemeAccent: (String) -> Unit,
    onSetSeasonOverride: (String) -> Unit,
    onSaveDetails: (academicLevel: String, petName: String, groupCode: String, ageYears: Long, degreeOrMaster: String) -> Unit,
    onLaunchPlayPurchase: (String) -> Unit,
    onRestorePlayPurchases: () -> Unit,
    onOpenConnectors: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onConnectGoogle: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    var academicLevel by remember(profile) { mutableStateOf(profile?.academicLevel ?: "Secundaria") }
    var petName by remember(profile) { mutableStateOf(profile?.petName ?: "Bubo") }
    var groupCode by remember(profile) { mutableStateOf(profile?.studyGroupCode ?: "") }
    var ageText by remember(profile) { mutableStateOf((profile?.ageYears ?: 16L).toString()) }
    var degreeOrMaster by remember(profile) { mutableStateOf(profile?.degreeOrMaster ?: "") }

    val accents = listOf(
        "indigo" to "Índigo",
        "emerald" to "Esmeralda",
        "amber" to "Ámbar",
        "obsidian" to "Obsidiana"
    )
    val levels = listOf("Primaria", "Secundaria", "Preparatoria", "Universidad", "Posgrado")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personalizar y Vincular Cuenta") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isVisitorMode) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Estás en Modo Visitante", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Tus cambios se prueban localmente. Conecta tu cuenta de Google para sincronizar en la nube.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(onClick = onConnectGoogle, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Conectar con Google")
                            }
                        }
                    }
                }

                Text(
                    text = "Modo de Sesión",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = profile?.sessionMode != "numeric",
                        onClick = { onSetSessionMode("gamification") },
                        label = { Text("Gamificación") }
                    )
                    FilterChip(
                        selected = profile?.sessionMode == "numeric",
                        onClick = { onSetSessionMode("numeric") },
                        label = { Text("Numérico") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tema Oscuro", style = MaterialTheme.typography.labelLarge)
                    Switch(checked = isDarkTheme, onCheckedChange = { onToggleDarkTheme() })
                }

                Text("Color de Acento:", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(accents) { (key, label) ->
                        FilterChip(
                            selected = (profile?.themeAccent ?: "indigo") == key,
                            onClick = { onSetThemeAccent(key) },
                            label = { Text(label) }
                        )
                    }
                }

                Text("Clima / Tema Navidad:", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SeasonalThemeManager.seasonOptions) { (key, label) ->
                        FilterChip(
                            selected = (profile?.weatherSeasonOverride ?: "auto") == key,
                            onClick = { onSetSeasonOverride(key) },
                            label = { Text(label) }
                        )
                    }
                }

                Text("Nivel Académico:", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(levels) { lvl ->
                        FilterChip(
                            selected = academicLevel == lvl,
                            onClick = { academicLevel = lvl },
                            label = { Text(lvl) }
                        )
                    }
                }

                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Edad (18+ activa Plan Alto)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = degreeOrMaster,
                    onValueChange = { degreeOrMaster = it },
                    label = { Text("Carrera, Maestría o Área (Plan Alto)") },
                    placeholder = { Text("Ej. Maestría en Ciencia de Datos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text("Nombre de tu Mascota") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = groupCode,
                    onValueChange = { groupCode = it },
                    label = { Text("Código para vincular grupo/clase") },
                    placeholder = { Text("Ej. SEC-3B-2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenConnectors()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("settings_open_connectors")
                ) {
                    Icon(Icons.Filled.Key, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Claves API (Gemini, Cloud Code y Otros)")
                }

                if (isAdmin) {
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenAdminPanel()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("settings_open_admin")
                    ) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Abrir Modo Administrador")
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AllInclusive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Google Play · Membresías Protón 2",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Tickets disponibles hoy: ${profile?.ticketsRemaining ?: 15}.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(
                            onClick = { onLaunchPlayPurchase(PlayBillingManager.SKU_EXTRA_25) },
                            enabled = profile?.hasProtonExtra25 != true,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (profile?.hasProtonExtra25 == true) "Extra Protón 2 Activo ($25)" else "Extra Protón 2 ($25 MXN)"
                            )
                        }
                        OutlinedButton(
                            onClick = { onLaunchPlayPurchase(PlayBillingManager.SKU_LIFETIME_250) },
                            enabled = profile?.hasLifetimePass != true,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (profile?.hasLifetimePass == true) "Pase Único Activo ($250)" else "Pase Único Vitalicio ($250 MXN)"
                            )
                        }
                        OutlinedButton(
                            onClick = { onLaunchPlayPurchase(PlayBillingManager.SKU_MONTHLY_AGENT) },
                            enabled = profile?.hasMonthlyAgentSub != true,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (profile?.hasMonthlyAgentSub == true) "Agente Mensual Activo" else "Agente Mensual ($149 MXN/mes)"
                            )
                        }
                        OutlinedButton(
                            onClick = onRestorePlayPurchases,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Restore, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Restaurar Compras Google Play")
                        }
                    }
                }

                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isVisitorMode) "Salir del Modo Visitante" else "Cerrar sesión de Google")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ageVal = ageText.toLongOrNull() ?: (profile?.ageYears ?: 16L)
                    onSaveDetails(academicLevel, petName, groupCode, ageVal, degreeOrMaster)
                    onDismiss()
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
