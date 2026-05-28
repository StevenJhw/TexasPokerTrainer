package com.pokertrainer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokertrainer.app.ui.theme.*

enum class RangePosition(val label: String, val description: String) {
    UTG("UTG", "早位 ~15%"),
    MP("MP", "中间位 ~20%"),
    CO("CO", "关煞位 ~28%"),
    BTN("BTN", "庄家位 ~40%"),
    VS_RAISE("vs加注", "面对加注"),
    VS_3BET("vs3-bet", "面对3-bet")
}

private val RANKS = listOf("A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2")

private enum class HandTier(val color: Color, val label: String) {
    PREMIUM(Color(0xFFE74C3C), "顶级"),
    STRONG(Color(0xFFF39C12), "强牌"),
    PLAYABLE(Color(0xFF2ECC71), "可打"),
    MARGINAL(Color(0xFF3498DB), "边缘"),
    FOLD(Color(0xFF2C3E50), "弃牌")
}

private fun getHandLabel(row: Int, col: Int): String {
    val r1 = RANKS[row]
    val r2 = RANKS[col]
    return when {
        row == col -> "$r1$r2"
        row < col -> "$r1${r2}s"
        else -> "$r2${r1}o"
    }
}

private fun getHandTierForPosition(row: Int, col: Int, position: RangePosition): HandTier {
    val hand = getHandLabel(row, col)
    val isPair = row == col
    val isSuited = row < col

    val premium = setOf("AA", "KK", "QQ", "AKs", "AKo")
    val strong = setOf("JJ", "TT", "AQs", "AQo", "AJs", "KQs")
    val playable = setOf("99", "88", "77", "ATs", "AJo", "KJs", "KQo", "QJs", "JTs")
    val marginal = setOf("66", "55", "44", "33", "22", "A9s", "A8s", "KTs", "QTs", "T9s", "98s", "87s", "76s")

    val tier = when {
        hand in premium -> 1
        hand in strong -> 2
        hand in playable -> 3
        hand in marginal -> 4
        else -> 5
    }

    return when (position) {
        RangePosition.UTG -> when {
            tier <= 2 -> if (tier == 1) HandTier.PREMIUM else HandTier.STRONG
            else -> HandTier.FOLD
        }
        RangePosition.MP -> when {
            tier == 1 -> HandTier.PREMIUM
            tier == 2 -> HandTier.STRONG
            tier == 3 -> HandTier.PLAYABLE
            else -> HandTier.FOLD
        }
        RangePosition.CO -> when {
            tier == 1 -> HandTier.PREMIUM
            tier == 2 -> HandTier.STRONG
            tier == 3 -> HandTier.PLAYABLE
            tier == 4 -> HandTier.MARGINAL
            else -> HandTier.FOLD
        }
        RangePosition.BTN -> when {
            tier == 1 -> HandTier.PREMIUM
            tier == 2 -> HandTier.STRONG
            tier == 3 -> HandTier.PLAYABLE
            tier == 4 -> HandTier.MARGINAL
            else -> HandTier.FOLD
        }
        RangePosition.VS_RAISE -> when {
            hand in setOf("AA", "KK", "AKs") -> HandTier.PREMIUM
            hand in setOf("QQ", "AKo") -> HandTier.STRONG
            tier == 2 -> HandTier.PLAYABLE
            tier == 3 && isSuited -> HandTier.MARGINAL
            else -> HandTier.FOLD
        }
        RangePosition.VS_3BET -> when {
            hand in setOf("AA", "KK") -> HandTier.PREMIUM
            hand in setOf("QQ", "AKs") -> HandTier.STRONG
            else -> HandTier.FOLD
        }
    }
}

@Composable
fun HandRangeChart(modifier: Modifier = Modifier) {
    var selectedPosition by remember { mutableStateOf(RangePosition.BTN) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Position selector
        Text(
            text = "选择位置查看范围：",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RangePosition.entries.forEach { pos ->
                val isSelected = pos == selectedPosition
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedPosition = pos },
                    label = {
                        Text(
                            text = pos.label,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentGreen,
                        selectedLabelColor = FeltDark,
                        containerColor = FeltMid,
                        labelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = selectedPosition.description,
            style = MaterialTheme.typography.labelSmall,
            color = AccentGold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HandTier.entries.forEach { tier ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(tier.color, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = tier.label,
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 13x13 grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FeltLight, RoundedCornerShape(4.dp))
        ) {
            for (row in 0 until 13) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 13) {
                        val tier = getHandTierForPosition(row, col, selectedPosition)
                        val label = getHandLabel(row, col)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(tier.color)
                                .border(0.5.dp, FeltDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 7.sp,
                                color = Color.White,
                                fontWeight = if (tier != HandTier.FOLD) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                lineHeight = 8.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Count playable hands
        var playCount = 0
        for (row in 0 until 13) {
            for (col in 0 until 13) {
                if (getHandTierForPosition(row, col, selectedPosition) != HandTier.FOLD) {
                    playCount++
                }
            }
        }
        val percentage = (playCount * 100) / 169

        Text(
            text = "可打手牌：$playCount / 169 种 ($percentage%)",
            style = MaterialTheme.typography.labelMedium,
            color = AccentGreen
        )
    }
}
