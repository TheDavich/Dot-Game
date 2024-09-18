package com.alpha.dots.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
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
import com.alpha.dots.util.MULTIPLAYER_GAME_SCREEN
import com.alpha.dots.util.SETTINGS_SCREEN
import com.alpha.dots.util.SINGLE_PLAYER_GAME_SCREEN

@Composable
fun MainMenu(
    viewModel: GameViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
                    .statusBarsPadding()
                    .clickable {
                        navController.navigate(SETTINGS_SCREEN)
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        MainMenuScreenContent(
            onSinglePlayerClicked = {
                viewModel.resetGameState()
                navController.navigate(SINGLE_PLAYER_GAME_SCREEN)
            },
            modifier = modifier.padding(innerPadding)
        )
    }
}

@Composable
fun MainMenuScreenContent(
    onSinglePlayerClicked: () -> Unit,
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
    )
}