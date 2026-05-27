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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokertrainer.app.data.model.Position
import com.pokertrainer.app.ui.theme.*

@Composable
fun StatsScreen(viewModel: TrainingViewModel) {
    val state by viewModel.state.collectAsState()
    val progress = state.progress

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FeltDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        var showResetConfirm by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "你的进度",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Text(
                    text = "追踪你的学习进度",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            TextButton(onClick = { showResetConfirm = true }) {
                Text("重置", color = AccentRed)
            }
        }

        if (showResetConfirm) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IncorrectBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("确定重置所有进度？", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showResetConfirm = false },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("取消", color = TextSecondary)
                        }
                        Button(
                            onClick = {
                                viewModel.resetProgress()
                                showResetConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("确认重置", color = TextPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Big accuracy display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FeltMid),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${(progress.overallAccuracy * 100).toInt()}%",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    color = AccentGreen
                )
                Text(
                    text = "总正确率",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(value = "${progress.totalHands}", label = "总手数")
                    StatItem(value = "${progress.correctHands}", label = "正确")
                    StatItem(value = "${progress.streak}", label = "最长连胜")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // By position
        Text(
            text = "按位置统计",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FeltMid),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Position.entries.forEach { position ->
                    val stats = progress.accuracyByPosition[position]
                    val total = stats?.first ?: 0
                    val correct = stats?.second ?: 0
                    val accuracy = if (total > 0) (correct * 100 / total) else 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = position.shortName,
                            style = MaterialTheme.typography.labelLarge,
                            color = AccentGold,
                            modifier = Modifier.width(40.dp)
                        )
                        LinearProgressIndicator(
                            progress = { accuracy / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp),
                            color = when {
                                accuracy >= 80 -> AccentGreen
                                accuracy >= 50 -> AccentGold
                                total == 0 -> FeltLight
                                else -> AccentRed
                            },
                            trackColor = FeltLight
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (total > 0) "$accuracy%" else "—",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.width(40.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Readiness
        val readiness = when {
            progress.overallAccuracy >= 0.8f && progress.totalHands >= 50 -> Triple("可以上桌了！", "你已准备好去打$60锦标赛", AccentGreen)
            progress.overallAccuracy >= 0.6f && progress.totalHands >= 20 -> Triple("快了", "继续练习，你在进步", AccentGold)
            else -> Triple("继续练习", "多做几手建立信心", TextSecondary)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FeltMid),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = readiness.first,
                    style = MaterialTheme.typography.headlineMedium,
                    color = readiness.third
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = readiness.second,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}
