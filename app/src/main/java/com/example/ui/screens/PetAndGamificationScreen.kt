package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.SeasonalThemeManager
import com.example.data.model.GamificationMission
import com.example.data.model.SeasonalThemeInfo
import com.example.data.model.StudentProfile
import com.example.ui.theme.AutumnCempasuchil
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PetAccentEmerald
import com.example.ui.theme.PetAccentGold
import com.example.ui.theme.PetAccentIndigo
import com.example.ui.theme.PetAccentRose
import com.example.ui.theme.RainStormPrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.SunnySolarGold
import com.example.ui.theme.XmasCrimson

private data class PetAccessoryOption(
    val id: String,
    val label: String,
    val coinCost: Long
)

private data class BadgeMeta(
    val id: String,
    val title: String,
    val subtitle: String
)

@Composable
fun PetAndGamificationTab(
    profile: StudentProfile?,
    missions: List<GamificationMission>,
    seasonalInfo: SeasonalThemeInfo,
    onUpdatePetConfig: (name: String, species: String, accessory: String, accent: String, coinCost: Long) -> Unit,
    onApplyBaseConfig: (species: String) -> Unit,
    onFeedPet: () -> Unit,
    onUnlockBadge: (badgeId: String, title: String) -> Unit,
    onSelectSeason: (String) -> Unit,
    onClaimSeasonalEgg: () -> Unit
) {
    val speciesOptions = listOf(
        "autosauro" to "Autosauro Rex",
        "buho" to "Búho Sabio",
        "zorro" to "Zorro Ágil",
        "dragon" to "Dragón Lógico",
        "gato" to "Gato Cyber"
    )
    val accessoryOptions = listOf(
        PetAccessoryOption("ninguno", "Natural", 0L),
        PetAccessoryOption("lentes", "Lentes Pro", 0L),
        PetAccessoryOption("birrete", "Birrete (30m)", 30L),
        PetAccessoryOption("audifonos", "Audífonos (40m)", 40L),
        PetAccessoryOption("corona", "Corona (80m)", 80L)
    )
    val accentOptions = listOf(
        "indigo" to "Índigo",
        "esmeralda" to "Esmeralda",
        "dorado" to "Dorado",
        "rosa" to "Rosa Neón"
    )
    val badges = listOf(
        BadgeMeta("inicio", "Primer Despegue", "Iniciaste tu camino en MentePro"),
        BadgeMeta("estilo_mascota", "Estilista de Mascota", "Personalizaste tu compañero IA"),
        BadgeMeta("cazador_easter_eggs", "Explorador Protón 2", "Descubriste el Easter Egg oculto Protón 2"),
        BadgeMeta("huevo_navidad", "Espíritu Navideño", "Abriste el Huevecillo Esfera Navideña"),
        BadgeMeta("camara_pro", "Visión Académica", "Resolviste tareas o apuntes con cámara"),
        BadgeMeta("creador_codigo", "Creador Digital", "Generaste una app o videojuego IA")
    )

    var draftName by remember(profile?.petName) { mutableStateOf(profile?.petName ?: "Bubo") }
    var draftSpecies by remember(profile?.petSpecies) { mutableStateOf(profile?.petSpecies ?: "buho") }
    var draftAccessory by remember(profile?.petAccessory) { mutableStateOf(profile?.petAccessory ?: "ninguno") }
    var draftAccent by remember(profile?.petColorAccent) { mutableStateOf(profile?.petColorAccent ?: "indigo") }

    val selectedAccCost = accessoryOptions.firstOrNull { it.id == draftAccessory }?.coinCost ?: 0L
    val effectiveCost = if (draftAccessory == profile?.petAccessory) 0L else selectedAccCost

    val level = profile?.petLevel ?: 1L
    val xp = profile?.petXp ?: 0L
    val maxXp = (profile?.xpForNextLevel ?: 100L).coerceAtLeast(1L)
    val progress = (xp.toFloat() / maxXp.toFloat()).coerceIn(0f, 1f)
    val coins = profile?.studyCoins ?: 0L
    val unlocked = profile?.unlockedBadges ?: listOf("inicio")
    val eggClaimed = unlocked.contains("huevo_${seasonalInfo.seasonId}")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Pet Configurator Preview Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Configurador de Mascotas",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "$coins monedas",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PetAvatarCanvas(
                            species = draftSpecies,
                            accessory = draftAccessory,
                            accentKey = draftAccent,
                            seasonId = seasonalInfo.seasonId,
                            modifier = Modifier.size(116.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${draftName.ifBlank { "Bubo" }} · Nv. $level",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = profile?.petStageTitle ?: "Explorador Inicial",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                            )
                            Text(
                                text = "$xp / $maxXp XP · Racha ${profile?.streakDays ?: 1} días",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        label = { Text("Nombre de tu mascota") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pet_name_input")
                    )

                    Text("Especie:", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(speciesOptions) { (id, label) ->
                            FilterChip(
                                selected = draftSpecies == id,
                                onClick = { draftSpecies = id },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text("Color de Aura:", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(accentOptions) { (id, label) ->
                            FilterChip(
                                selected = draftAccent == id,
                                onClick = { draftAccent = id },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text("Accesorio:", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(accessoryOptions) { acc ->
                            FilterChip(
                                selected = draftAccessory == acc.id,
                                onClick = { draftAccessory = acc.id },
                                label = { Text(acc.label) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onUpdatePetConfig(draftName, draftSpecies, draftAccessory, draftAccent, effectiveCost)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_pet_config_button")
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (effectiveCost > 0L) "Guardar (-$effectiveCost m)" else "Guardar Mascota"
                            )
                        }
                        FilledTonalButton(
                            onClick = onFeedPet,
                            modifier = Modifier.testTag("feed_pet_button")
                        ) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("+60 XP (20m)")
                        }
                    }

                    // Configuración Mascota Base
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Configuración Mascota Base (Protón 2)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Activa al instante el perfil base de Autosauro Rex o restablece la mascota inicial sin costo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = {
                                        draftSpecies = "autosauro"
                                        draftAccessory = "ninguno"
                                        draftAccent = "esmeralda"
                                        onApplyBaseConfig("autosauro")
                                    },
                                    modifier = Modifier.weight(1f).testTag("base_autosauro_button")
                                ) {
                                    Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Base Autosauro")
                                }
                                OutlinedButton(
                                    onClick = {
                                        draftSpecies = "buho"
                                        draftAccessory = "ninguno"
                                        draftAccent = "indigo"
                                        onApplyBaseConfig("buho")
                                    },
                                    modifier = Modifier.weight(1f).testTag("base_reset_button")
                                ) {
                                    Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Base Clásica")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1.5 Huevecillos según el Clima y Navidad (Easter Eggs)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (seasonalInfo.isChristmasMode) Icons.Filled.AcUnit else Icons.Filled.WbSunny,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Huevecillos del Clima y Easter Eggs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(seasonalInfo.title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface) {
                            Text(
                                text = seasonalInfo.badgeText,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(seasonalInfo.bannerMessage, style = MaterialTheme.typography.bodyMedium)

                    Text("Cambiar Clima / Temporada:", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SeasonalThemeManager.seasonOptions) { (key, label) ->
                            FilterChip(
                                selected = (profile?.weatherSeasonOverride ?: "auto") == key,
                                onClick = { onSelectSeason(key) },
                                label = { Text(label) }
                            )
                        }
                    }

                    Button(
                        onClick = onClaimSeasonalEgg,
                        enabled = !eggClaimed,
                        modifier = Modifier.fillMaxWidth().testTag("claim_seasonal_egg_button")
                    ) {
                        Icon(Icons.Filled.Egg, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (eggClaimed) {
                                "${seasonalInfo.eggName} ya eclosionado"
                            } else {
                                "Abrir ${seasonalInfo.eggName} (${seasonalInfo.eggRewardNote})"
                            }
                        )
                    }
                }
            }
        }

        // 2. Gamification Hub: Missions & Streak
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = StatusWarning)
                            Spacer(Modifier.width(8.dp))
                            Text("Módulo de Gamificación", style = MaterialTheme.typography.titleLarge)
                        }
                        Text(
                            text = "${missions.count { it.completed }}/${missions.size} metas",
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Completa misiones reales en tus materias, cámara, Plan Alto y código IA para ganar XP, monedas y medallas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    missions.forEach { mission ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (mission.completed) {
                                StatusSuccess.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (mission.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (mission.completed) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mission.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = mission.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = mission.progressText,
                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily)
                                    )
                                    Text(
                                        text = "+${mission.xpReward} XP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Medallas y Logros
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(8.dp))
                        Text("Vitrina de Medallas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    badges.forEach { badge ->
                        val isEarned = unlocked.contains(badge.id)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isEarned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isEarned) { onUnlockBadge(badge.id, badge.title) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = if (isEarned) Icons.Filled.Stars else Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isEarned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(badge.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                        Text(badge.subtitle, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                if (!isEarned) {
                                    OutlinedButton(onClick = { onUnlockBadge(badge.id, badge.title) }) {
                                        Text("Reclamar")
                                    }
                                } else {
                                    Text(
                                        "Obtenida",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
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

@Composable
fun PetAvatarCanvas(
    species: String,
    accessory: String,
    accentKey: String,
    seasonId: String = "otono",
    modifier: Modifier = Modifier
) {
    val accentColor = when (accentKey) {
        "esmeralda" -> PetAccentEmerald
        "dorado" -> PetAccentGold
        "rosa" -> PetAccentRose
        else -> PetAccentIndigo
    }
    val surfaceContrast = MaterialTheme.colorScheme.surface
    val darkStroke = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(accentColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h * 0.56f
            val radius = w * 0.34f

            // Outer aura ring
            drawCircle(
                color = accentColor.copy(alpha = 0.25f),
                radius = radius * 1.25f,
                center = Offset(cx, cy)
            )

            // Seasonal ambient particles around avatar
            when (seasonId) {
                "navidad" -> {
                    drawCircle(Color.White, radius = 3.5f, center = Offset(w * 0.16f, h * 0.20f))
                    drawCircle(Color.White, radius = 2.8f, center = Offset(w * 0.84f, h * 0.24f))
                    drawCircle(Color.White, radius = 3.0f, center = Offset(w * 0.22f, h * 0.84f))
                }
                "lluvia" -> {
                    drawCircle(RainStormPrimary, radius = 3.2f, center = Offset(w * 0.18f, h * 0.24f))
                    drawCircle(RainStormPrimary, radius = 3.2f, center = Offset(w * 0.82f, h * 0.30f))
                }
                "soleado" -> {
                    drawCircle(SunnySolarGold, radius = 4.5f, center = Offset(w * 0.84f, h * 0.18f))
                }
                else -> {
                    drawCircle(AutumnCempasuchil, radius = 3.4f, center = Offset(w * 0.18f, h * 0.24f))
                    drawCircle(AutumnCempasuchil, radius = 3.4f, center = Offset(w * 0.82f, h * 0.78f))
                }
            }

            // Ears / horns / dorsal plates depending on species
            when (species) {
                "autosauro" -> {
                    // Cyber dino crest plates + mechanic fin
                    listOf(-0.48f, 0f, 0.48f).forEachIndexed { idx, offset ->
                        val spike = Path().apply {
                            val peakY = if (idx == 1) cy - radius * 1.38f else cy - radius * 1.20f
                            moveTo(cx + radius * (offset - 0.18f), cy - radius * 0.76f)
                            lineTo(cx + radius * offset, peakY)
                            lineTo(cx + radius * (offset + 0.18f), cy - radius * 0.76f)
                            close()
                        }
                        drawPath(spike, color = if (idx == 1) PetAccentGold else PetAccentEmerald)
                    }
                    // Mechanical wheel badge on side
                    drawCircle(PetAccentGold, radius = radius * 0.22f, center = Offset(cx - radius * 0.92f, cy + radius * 0.48f))
                    drawCircle(darkStroke, radius = radius * 0.10f, center = Offset(cx - radius * 0.92f, cy + radius * 0.48f))
                }
                "zorro", "gato" -> {
                    val leftEar = Path().apply {
                        moveTo(cx - radius * 0.85f, cy - radius * 0.3f)
                        lineTo(cx - radius * 0.95f, cy - radius * 1.15f)
                        lineTo(cx - radius * 0.2f, cy - radius * 0.78f)
                        close()
                    }
                    val rightEar = Path().apply {
                        moveTo(cx + radius * 0.85f, cy - radius * 0.3f)
                        lineTo(cx + radius * 0.95f, cy - radius * 1.15f)
                        lineTo(cx + radius * 0.2f, cy - radius * 0.78f)
                        close()
                    }
                    drawPath(leftEar, color = accentColor)
                    drawPath(rightEar, color = accentColor)
                }
                "dragon" -> {
                    val leftHorn = Path().apply {
                        moveTo(cx - radius * 0.55f, cy - radius * 0.75f)
                        lineTo(cx - radius * 0.75f, cy - radius * 1.32f)
                        lineTo(cx - radius * 0.15f, cy - radius * 0.88f)
                        close()
                    }
                    val rightHorn = Path().apply {
                        moveTo(cx + radius * 0.55f, cy - radius * 0.75f)
                        lineTo(cx + radius * 0.75f, cy - radius * 1.32f)
                        lineTo(cx + radius * 0.15f, cy - radius * 0.88f)
                        close()
                    }
                    drawPath(leftHorn, color = PetAccentGold)
                    drawPath(rightHorn, color = PetAccentGold)
                }
                else -> { // buho tufts
                    drawCircle(accentColor, radius = radius * 0.28f, center = Offset(cx - radius * 0.65f, cy - radius * 0.72f))
                    drawCircle(accentColor, radius = radius * 0.28f, center = Offset(cx + radius * 0.65f, cy - radius * 0.72f))
                }
            }

            // Head / Body
            if (species == "autosauro") {
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(cx - radius, cy - radius * 0.92f),
                    size = Size(radius * 2f, radius * 1.84f),
                    cornerRadius = CornerRadius(radius * 0.45f, radius * 0.45f)
                )
            } else {
                drawCircle(
                    color = accentColor,
                    radius = radius,
                    center = Offset(cx, cy)
                )
            }

            // Belly / face patch
            drawCircle(
                color = surfaceContrast.copy(alpha = 0.88f),
                radius = radius * 0.68f,
                center = Offset(cx, cy + radius * 0.12f)
            )

            // Eyes
            val eyeY = cy - radius * 0.08f
            val eyeDx = radius * 0.34f
            if (species == "autosauro") {
                // Cyber visor eyes
                drawRoundRect(
                    color = darkStroke,
                    topLeft = Offset(cx - radius * 0.55f, eyeY - radius * 0.14f),
                    size = Size(radius * 1.10f, radius * 0.26f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawCircle(PetAccentEmerald, radius = radius * 0.07f, center = Offset(cx - eyeDx * 0.75f, eyeY))
                drawCircle(PetAccentEmerald, radius = radius * 0.07f, center = Offset(cx + eyeDx * 0.75f, eyeY))
            } else {
                drawCircle(darkStroke, radius = radius * 0.15f, center = Offset(cx - eyeDx, eyeY))
                drawCircle(darkStroke, radius = radius * 0.15f, center = Offset(cx + eyeDx, eyeY))
                drawCircle(Color.White, radius = radius * 0.05f, center = Offset(cx - eyeDx - 2f, eyeY - 2f))
                drawCircle(Color.White, radius = radius * 0.05f, center = Offset(cx + eyeDx - 2f, eyeY - 2f))
            }

            // Beak, snout or smile
            if (species == "buho") {
                val beak = Path().apply {
                    moveTo(cx - radius * 0.14f, cy + radius * 0.08f)
                    lineTo(cx + radius * 0.14f, cy + radius * 0.08f)
                    lineTo(cx, cy + radius * 0.30f)
                    close()
                }
                drawPath(beak, color = PetAccentGold)
            } else if (species == "autosauro") {
                drawRoundRect(
                    color = PetAccentGold,
                    topLeft = Offset(cx - radius * 0.28f, cy + radius * 0.18f),
                    size = Size(radius * 0.56f, radius * 0.18f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            } else {
                drawCircle(darkStroke, radius = radius * 0.10f, center = Offset(cx, cy + radius * 0.15f))
            }

            // Christmas Santa Hat Easter Egg when seasonId == "navidad"
            if (seasonId == "navidad" && accessory == "ninguno") {
                val hatBase = cy - radius * 0.85f
                val hatPath = Path().apply {
                    moveTo(cx - radius * 0.58f, hatBase)
                    lineTo(cx + radius * 0.08f, hatBase - radius * 0.72f)
                    lineTo(cx + radius * 0.58f, hatBase)
                    close()
                }
                drawPath(hatPath, color = XmasCrimson)
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(cx - radius * 0.64f, hatBase - radius * 0.10f),
                    size = Size(radius * 1.28f, radius * 0.22f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawCircle(Color.White, radius = radius * 0.14f, center = Offset(cx + radius * 0.12f, hatBase - radius * 0.74f))
            }

            // Accessory overlay
            when (accessory) {
                "lentes" -> {
                    drawRoundRect(
                        color = darkStroke,
                        topLeft = Offset(cx - eyeDx - radius * 0.22f, eyeY - radius * 0.20f),
                        size = Size(radius * 0.44f, radius * 0.38f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = darkStroke,
                        topLeft = Offset(cx + eyeDx - radius * 0.22f, eyeY - radius * 0.20f),
                        size = Size(radius * 0.44f, radius * 0.38f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawLine(
                        color = darkStroke,
                        start = Offset(cx - eyeDx + radius * 0.22f, eyeY),
                        end = Offset(cx + eyeDx - radius * 0.22f, eyeY),
                        strokeWidth = 4f
                    )
                    drawCircle(Color.White.copy(alpha = 0.35f), radius = radius * 0.09f, center = Offset(cx - eyeDx, eyeY))
                    drawCircle(Color.White.copy(alpha = 0.35f), radius = radius * 0.09f, center = Offset(cx + eyeDx, eyeY))
                }
                "birrete" -> {
                    val capTop = cy - radius * 1.15f
                    val diamond = Path().apply {
                        moveTo(cx, capTop - radius * 0.22f)
                        lineTo(cx + radius * 0.85f, capTop + radius * 0.08f)
                        lineTo(cx, capTop + radius * 0.34f)
                        lineTo(cx - radius * 0.85f, capTop + radius * 0.08f)
                        close()
                    }
                    drawPath(diamond, color = darkStroke)
                    drawLine(
                        color = PetAccentGold,
                        start = Offset(cx + radius * 0.72f, capTop + radius * 0.12f),
                        end = Offset(cx + radius * 0.72f, capTop + radius * 0.55f),
                        strokeWidth = 4f
                    )
                }
                "audifonos" -> {
                    drawArc(
                        color = darkStroke,
                        startAngle = 190f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(cx - radius * 1.05f, cy - radius * 1.05f),
                        size = Size(radius * 2.1f, radius * 2.1f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 7f)
                    )
                    drawRoundRect(
                        color = PetAccentGold,
                        topLeft = Offset(cx - radius * 1.16f, cy - radius * 0.25f),
                        size = Size(radius * 0.25f, radius * 0.52f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = PetAccentGold,
                        topLeft = Offset(cx + radius * 0.91f, cy - radius * 0.25f),
                        size = Size(radius * 0.25f, radius * 0.52f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
                "corona" -> {
                    val base = cy - radius * 0.88f
                    val crown = Path().apply {
                        moveTo(cx - radius * 0.55f, base)
                        lineTo(cx - radius * 0.45f, base - radius * 0.45f)
                        lineTo(cx - radius * 0.15f, base - radius * 0.18f)
                        lineTo(cx, base - radius * 0.55f)
                        lineTo(cx + radius * 0.15f, base - radius * 0.18f)
                        lineTo(cx + radius * 0.45f, base - radius * 0.45f)
                        lineTo(cx + radius * 0.55f, base)
                        close()
                    }
                    drawPath(crown, color = PetAccentGold)
                }
            }
        }
    }
}
