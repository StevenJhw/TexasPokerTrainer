package com.pokertrainer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokertrainer.app.data.lessons
import com.pokertrainer.app.ui.components.CardView
import com.pokertrainer.app.ui.components.HandRangeChart
import com.pokertrainer.app.ui.theme.*
import kotlinx.coroutines.launch

data class Lesson(
    val id: Int,
    val title: String,
    val subtitle: String,
    val icon: String,
    val content: List<LessonPage>
)

data class LessonPage(
    val title: String,
    val body: String,
    val highlight: String = "",
    val exampleCards: List<List<com.pokertrainer.app.data.model.Card>> = emptyList(),
    val exampleLabels: List<String> = emptyList(),
    val showRangeChart: Boolean = false
)

@Composable
fun LearnScreen(
    selectedLessonId: Int,
    currentPage: Int,
    onLessonIdChange: (Int) -> Unit,
    onPageChange: (Int) -> Unit,
    onStartPractice: () -> Unit
) {
    val selectedLesson = lessons.firstOrNull { it.id == selectedLessonId }

    if (selectedLesson != null) {
        LessonDetailScreen(
            lesson = selectedLesson,
            initialPage = currentPage,
            onPageChange = onPageChange,
            onClose = {
                onLessonIdChange(-1)
                onPageChange(0)
            }
        )
    } else {
        LessonListScreen(
            onLessonSelect = { lesson ->
                onPageChange(0)
                onLessonIdChange(lesson.id)
            },
            onStartPractice = onStartPractice
        )
    }
}

@Composable
private fun LessonListScreen(
    onLessonSelect: (Lesson) -> Unit,
    onStartPractice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FeltDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "学习德州扑克",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Text(
            text = "上桌前掌握基础知识",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        lessons.forEach { lesson ->
            LessonCard(lesson = lesson, onClick = { onLessonSelect(lesson) })
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStartPractice,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "开始练习",
                style = MaterialTheme.typography.labelLarge,
                color = FeltDark
            )
        }
    }
}

@Composable
private fun LessonCard(lesson: Lesson, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FeltMid),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(FeltLight, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = lesson.icon, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = lesson.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Text(
                text = "${lesson.content.size} 页",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun LessonDetailScreen(
    lesson: Lesson,
    initialPage: Int,
    onPageChange: (Int) -> Unit,
    onClose: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { lesson.content.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val currentPage = pagerState.settledPage
    val isLastPage = currentPage == lesson.content.size - 1
    val isFirstPage = currentPage == 0

    // Sync page changes back to parent
    LaunchedEffect(currentPage) {
        onPageChange(currentPage)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FeltDark)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = FeltMid),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("关闭", color = TextPrimary)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${currentPage + 1} / ${lesson.content.size}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { (currentPage + 1).toFloat() / lesson.content.size },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = AccentGreen,
            trackColor = FeltMid
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Swipe hint
        Text(
            text = "左右滑动翻页",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            val page = lesson.content[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    lineHeight = 26.sp
                )

                // Show example cards if present
                if (page.exampleCards.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    page.exampleCards.forEachIndexed { index, cards ->
                        if (index < page.exampleLabels.size) {
                            Text(
                                text = page.exampleLabels[index],
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            cards.forEach { card ->
                                CardView(card = card, small = true)
                            }
                        }
                    }
                }

                if (page.showRangeChart) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HandRangeChart()
                }

                if (page.highlight.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = FeltLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = page.highlight,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentGold,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bottom buttons — always visible
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left button: Close (first page) or Previous
            OutlinedButton(
                onClick = {
                    if (isFirstPage) {
                        onClose()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(currentPage - 1)
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isFirstPage) "关闭" else "< 上一页",
                    color = TextSecondary
                )
            }

            // Right button: Next or Done (last page)
            Button(
                onClick = {
                    if (isLastPage) {
                        onClose()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLastPage) AccentGreen else AccentBlue
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isLastPage) "完成" else "下一页 >",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isLastPage) FeltDark else TextPrimary
                )
            }
        }
    }
}
