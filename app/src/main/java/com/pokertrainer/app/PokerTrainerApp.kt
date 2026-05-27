package com.pokertrainer.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokertrainer.app.ui.screens.LearnScreen
import com.pokertrainer.app.ui.screens.StatsScreen
import com.pokertrainer.app.ui.screens.TrainingScreen
import com.pokertrainer.app.ui.screens.TrainingViewModel
import com.pokertrainer.app.ui.theme.*

enum class Screen(val title: String, val icon: ImageVector) {
    LEARN("学习", Icons.Default.Info),
    TRAINING("练习", Icons.Default.PlayArrow),
    STATS("统计", Icons.Default.Star)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokerTrainerApp() {
    var currentScreen by remember { mutableStateOf(Screen.LEARN) }
    val viewModel: TrainingViewModel = viewModel()

    // Persist Learn screen state across tab switches
    var learnLessonId by remember { mutableStateOf(-1) }
    var learnPage by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = FeltMid) {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentGreen,
                            selectedTextColor = AccentGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = FeltLight
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                Screen.LEARN -> LearnScreen(
                    selectedLessonId = learnLessonId,
                    currentPage = learnPage,
                    onLessonIdChange = { learnLessonId = it },
                    onPageChange = { learnPage = it },
                    onStartPractice = { currentScreen = Screen.TRAINING }
                )
                Screen.TRAINING -> TrainingScreen(viewModel = viewModel)
                Screen.STATS -> StatsScreen(viewModel = viewModel)
            }
        }
    }
}
