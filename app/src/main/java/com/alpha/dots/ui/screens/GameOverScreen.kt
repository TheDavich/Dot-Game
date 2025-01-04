package com.alpha.dots.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.alpha.dots.model.getReactionCategory
import com.alpha.dots.ui.composables.UniversalButton
import com.alpha.dots.ui.viewModel.GameViewModel
import com.alpha.dots.util.MAIN_MENU_SCREEN
import com.alpha.dots.util.SINGLE_PLAYER_GAME_SCREEN

@Composable
internal fun GameOverScreen(
    viewModel: GameViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()

    // Access the final score and reaction time from the game state
    val finalScore = gameState?.score ?: 0
    val avgReactionTime = gameState?.avgReactionTime ?: 0L // If reaction time not available, set to 0L

    // Get the reaction time category description
    val reactionCategory = getReactionCategory(avgReactionTime)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Game Over", color = Color.White, fontSize = 32.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Final Score: $finalScore",
            color = Color.White,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Average Reaction Time: $avgReactionTime ms",
            color = Color.White,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your reaction time:",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = reactionCategory,
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        UniversalButton(text = "Play Again") {
            viewModel.resetGameState() // Reset game state before starting a new game
            navController.navigate(SINGLE_PLAYER_GAME_SCREEN) {
                popUpTo(SINGLE_PLAYER_GAME_SCREEN) { inclusive = true } // Ensure to pop this screen as well
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        UniversalButton(text = "Main Menu") {
            navController.navigate(MAIN_MENU_SCREEN) {
                popUpTo(MAIN_MENU_SCREEN) { inclusive = true } // Ensure to pop all intermediate screens
            }
        }
    }
}

