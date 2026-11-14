package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AiDuelAgent
import com.example.data.model.DuelChallenge
import com.example.domain.SubjectCatalog
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.AiCallState
import com.example.ui.viewmodel.DuelSessionState

@Composable
fun AiDuelsTab(
    duelSession: DuelSessionState,
    duelChallengeState: AiCallState<DuelChallenge>,
    onStartDuel: (AiDuelAgent) -> Unit,
    onSubmitAnswer: (Int) -> Unit,
    onNextRound: () -> Unit,
    onResetDuel: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val activeAgent = duelSession.activeAgent
        if (activeAgent == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.SportsKabaddi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Arena de Duelos vs Agentes IA", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "Enfréntate a agentes especializados de Matemáticas, Ciencias, Artes o Programación en duelos de 3 rondas.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            items(SubjectCatalog.duelAgents, key = { it.id }) { agent ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(agent.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${agent.title} · ${agent.subject}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = agent.levelLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(agent.tagline, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(
                            onClick = { onStartDuel(agent) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("duel_agent_${agent.id}")
                        ) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Desafiar a ${agent.name}")
                        }
                    }
                }
            }
        } else {
            // Active Head-to-Head Scoreboard
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ronda ${duelSession.round} / ${duelSession.totalRounds}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            OutlinedButton(onClick = onResetDuel) {
                                Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Cambiar rival")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ScoreBadge(
                                label = "Tú",
                                score = duelSession.playerScore,
                                modifier = Modifier.weight(1f)
                            )
                            ScoreBadge(
                                label = activeAgent.name,
                                score = duelSession.agentScore,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                when (duelChallengeState) {
                    is AiCallState.Idle -> Unit
                    is AiCallState.Loading -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                Text("${activeAgent.name} está preparando un reto de ${activeAgent.subject}...")
                            }
                        }
                    }
                    is AiCallState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(duelChallengeState.message)
                                Button(onClick = onResetDuel) { Text("Volver") }
                            }
                        }
                    }
                    is AiCallState.Success -> {
                        val challenge = duelChallengeState.data
                        val chosen = duelSession.selectedOption
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "${activeAgent.name}: \"${challenge.agentTaunt}\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Text(challenge.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                challenge.options.forEachIndexed { idx, optText ->
                                    val isAnswered = chosen != null
                                    val isCorrect = idx == challenge.correctIndex
                                    val isPlayerPick = idx == chosen
                                    val isAgentPick = idx == challenge.agentChoiceIndex

                                    val bg = when {
                                        !isAnswered -> MaterialTheme.colorScheme.surfaceVariant
                                        isCorrect -> StatusSuccess.copy(alpha = 0.2f)
                                        isPlayerPick -> StatusDanger.copy(alpha = 0.2f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(bg)
                                            .clickable(enabled = !isAnswered) { onSubmitAnswer(idx) }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = optText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isAnswered) {
                                            val tag = buildString {
                                                if (isPlayerPick) append("Tú ")
                                                if (isAgentPick) append("IA")
                                            }.trim()
                                            if (tag.isNotEmpty()) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = tag,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (chosen != null) {
                                    Text(
                                        text = "Explicación: ${challenge.explanation}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    if (duelSession.isFinished) {
                                        val won = duelSession.playerScore >= duelSession.agentScore
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (won) StatusSuccess.copy(alpha = 0.16f) else MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.EmojiEvents, contentDescription = null)
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = if (won) {
                                                        "¡Victoria ${duelSession.playerScore}-${duelSession.agentScore}! Ganaste +60 XP y +1 ticket."
                                                    } else {
                                                        "Resultado final ${duelSession.playerScore}-${duelSession.agentScore}. ¡Sigue practicando!"
                                                    },
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                            }
                                        }
                                        Button(onClick = onResetDuel, modifier = Modifier.fillMaxWidth()) {
                                            Text("Jugar otro duelo")
                                        }
                                    } else {
                                        FilledTonalButton(onClick = onNextRound, modifier = Modifier.fillMaxWidth()) {
                                            Text("Siguiente Ronda (${duelSession.round + 1}/${duelSession.totalRounds})")
                                        }
                                    }
                                }
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
private fun ScoreBadge(label: String, score: Int, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            Text(
                text = "$score pts",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
