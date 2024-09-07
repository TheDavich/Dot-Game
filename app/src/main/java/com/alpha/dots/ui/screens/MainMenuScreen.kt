package com.alpha.dots.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpha.dots.ui.composables.UniversalButton
import com.alpha.dots.ui.viewModel.GameViewModel
import com.alpha.dots.util.SinglePlayerGameScreen

@Composable
fun MainMenu(
    viewModel: GameViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->
        MainMenuScreenContent(
            onSinglePlayerClicked = {
                navController.navigate(SinglePlayerGameScreen)
            },
            onMatchmakingClicked = {
                /* Navigate to Matchmaking */
            },
            modifier = modifier
                .padding(innerPadding)
        )
    }
}

@Composable
fun MainMenuScreenContent(
    onSinglePlayerClicked: () -> Unit,
    onMatchmakingClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        UniversalButton(
            text = "Singleplayer",
            onClick = onSinglePlayerClicked
        )
        UniversalButton(
            text = "Matchmaking",
            onClick = onMatchmakingClicked
        )
        UniversalButton(
            text = "Rankings",
            onClick = { /* Navigate to Rankings */ }
        )
    }
}

@Preview
@Composable
fun MainMenuPreview() {
    MainMenuScreenContent(
        onSinglePlayerClicked = { /* Handle singleplayer click */ },
        onMatchmakingClicked = { /* Handle matchmaking click */ }
    )
}