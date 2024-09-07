package com.alpha.dots.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpha.dots.ui.composables.UniversalButton
import com.alpha.dots.ui.theme.Typography
import com.alpha.dots.ui.viewModel.GameViewModel
import com.alpha.dots.util.MainMenuScreen
import com.alpha.dots.util.SinglePlayerGameOverScreen
import com.alpha.dots.util.SinglePlayerGameScreen


@Composable
fun GameOverScreen(
    viewModel: GameViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val gameState by viewModel.gameState.collectAsState()
    val avgReactionTime = rememberSaveable { gameState?.avgReactionTime ?: 0L }

    BackHandler {}

    Scaffold(
        containerColor = Color.Black,
        modifier = modifier
            .fillMaxSize()
    ) { innerPadding ->
        GameOverScreenContent(
            avgReactionTime = avgReactionTime,
            onRestartClicked = {
                viewModel.startNewGame()
                navController.navigate(SinglePlayerGameScreen) {
                    popUpTo(SinglePlayerGameOverScreen) { inclusive = true }
                }
            },
            onMainMenuClicked = {
                navController.navigate(MainMenuScreen) {
                    popUpTo(SinglePlayerGameOverScreen) { inclusive = true }
                }
            },
            modifier = modifier
                .padding(innerPadding)
        )
    }
}

@Composable
fun GameOverScreenContent(
    avgReactionTime: Long,
    onRestartClicked: () -> Unit,
    onMainMenuClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Average Reaction Time: $avgReactionTime ms",
            style = Typography.titleLarge,
            color = Color.White
        )
        UniversalButton(
            text = "Restart",
            onClick = onRestartClicked
        )
        UniversalButton(
            text = "Main Menu",
            onClick = onMainMenuClicked
        )
    }
}

@Preview
@Composable
fun GameOverScreenPreview() {
    GameOverScreenContent(
        avgReactionTime = 1000,
        onRestartClicked = { /* Handle restart click */ },
        onMainMenuClicked = { /* Handle main menu click */ }
    )
}
