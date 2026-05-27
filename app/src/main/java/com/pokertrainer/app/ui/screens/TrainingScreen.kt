package com.pokertrainer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokertrainer.app.data.model.ActionType
import com.pokertrainer.app.data.model.RaiseSize
import com.pokertrainer.app.data.model.TableSize
import com.pokertrainer.app.ui.components.CardView
import com.pokertrainer.app.ui.theme.*

@Composable
fun TrainingScreen(viewModel: TrainingViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val scenario = state.currentScenario ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FeltDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Table size selector
        var showTablePicker by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = { showTablePicker = !showTablePicker },
                label = { Text("${state.tableSize.players} 人桌", color = TextPrimary) },
                shape = RoundedCornerShape(8.dp),
                colors = AssistChipDefaults.assistChipColors(containerColor = FeltMid)
            )
            Spacer(modifier = Modifier.weight(1f))
            StatChip(label = "连胜", value = "${state.currentStreak}", color = AccentGold)
            Spacer(modifier = Modifier.width(16.dp))
            StatChip(label = "正确率", value = "${(state.progress.overallAccuracy * 100).toInt()}%", color = AccentGreen)
            Spacer(modifier = Modifier.width(16.dp))
            StatChip(label = "手数", value = "${state.progress.totalHands}", color = AccentBlue)
        }

        if (showTablePicker) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FeltMid),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    TableSize.entries.forEach { size ->
                        val isSelected = size == state.tableSize
                        TextButton(
                            onClick = {
                                viewModel.setTableSize(size)
                                showTablePicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = size.display,
                                color = if (isSelected) AccentGreen else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Your hand
        Text(
            text = "你的手牌",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CardView(card = scenario.heroHand.first)
            CardView(card = scenario.heroHand.second)
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Scenario info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FeltMid),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                InfoRow(label = "桌子", value = "${state.tableSize.players} 人")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = FeltLight)
                InfoRow(label = "位置", value = "${scenario.position.display} (${scenario.position.shortName})")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = FeltLight)
                InfoRow(label = "情况", value = scenario.villainAction.display)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = FeltLight)
                InfoRow(label = "底池", value = "$${scenario.potSize}")
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Action buttons
        if (!state.showResult && !state.showRaiseSizing) {
            Text(
                text = "你的选择？",
                style = MaterialTheme.typography.titleLarge,
                color = AccentGold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionButton(
                    text = "Fold",
                    color = ButtonFold,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.submitAction(ActionType.FOLD) }
                )
                ActionButton(
                    text = "Call",
                    color = ButtonCall,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.submitAction(ActionType.CALL) }
                )
                ActionButton(
                    text = "Raise",
                    color = ButtonRaise,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.submitAction(ActionType.RAISE) }
                )
            }
        }

        // Raise sizing step
        if (state.showRaiseSizing && !state.showResult) {
            Text(
                text = "加注多少？",
                style = MaterialTheme.typography.titleLarge,
                color = AccentGold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RaiseSize.entries.forEach { size ->
                    Button(
                        onClick = { viewModel.submitRaiseSize(size) },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FeltMid),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = size.display,
                                style = MaterialTheme.typography.labelLarge,
                                color = AccentGreen
                            )
                            Text(
                                text = size.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = { viewModel.cancelRaise() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("返回", color = TextSecondary)
                }
            }
        }

        // Result
        if (state.showResult) {
            val isCorrect = state.isCorrect == true
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) CorrectBg else IncorrectBg
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isCorrect) "正确！" else "错误",
                        color = if (isCorrect) CorrectText else IncorrectText,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "你选了: ${state.userAnswer?.display ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "最优解: ${state.optimalDecision}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentGold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = FeltLight)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FeltMid),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = state.rule,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentGold,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.dealNewHand() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "下一手",
                    style = MaterialTheme.typography.labelLarge,
                    color = FeltDark
                )
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
    }
}
