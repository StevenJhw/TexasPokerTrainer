package com.pokertrainer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokertrainer.app.data.model.Card
import com.pokertrainer.app.data.model.CardColor

@Composable
fun CardView(card: Card, modifier: Modifier = Modifier, small: Boolean = false) {
    val textColor = when (card.suit.color) {
        CardColor.RED -> Color(0xFFE74C3C)
        CardColor.BLACK -> Color(0xFF2C3E50)
    }

    val cardGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFDF8), Color(0xFFF5F0E8))
    )

    val defaultWidth = if (small) 48.dp else 76.dp
    val defaultHeight = if (small) 68.dp else 108.dp
    val rankSize = if (small) 20.sp else 32.sp
    val suitSize = if (small) 16.sp else 26.sp
    val cornerSize = if (small) 8.sp else 10.sp
    val cornerSuitSize = if (small) 7.sp else 9.sp
    val cornerPad = if (small) 2.dp else 4.dp
    val radius = if (small) 6.dp else 10.dp
    val shadowDp = if (small) 4.dp else 8.dp

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = defaultWidth, minHeight = defaultHeight)
            .shadow(shadowDp, RoundedCornerShape(radius))
            .background(cardGradient, RoundedCornerShape(radius))
            .border(1.dp, Color(0xFFD4C5A0), RoundedCornerShape(radius)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = card.rank.display,
                color = textColor,
                fontSize = rankSize,
                fontWeight = FontWeight.Black
            )
            Text(
                text = card.suit.symbol,
                color = textColor,
                fontSize = suitSize
            )
        }

        // Top-left corner
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(cornerPad),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.rank.display,
                color = textColor,
                fontSize = cornerSize,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = card.suit.symbol,
                color = textColor,
                fontSize = cornerSuitSize
            )
        }

        // Bottom-right corner
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(cornerPad),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.suit.symbol,
                color = textColor,
                fontSize = cornerSuitSize
            )
            Text(
                text = card.rank.display,
                color = textColor,
                fontSize = cornerSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
