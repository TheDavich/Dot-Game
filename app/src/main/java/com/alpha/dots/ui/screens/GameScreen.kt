package com.alpha.dots.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.alpha.dots.model.Dot
import com.alpha.dots.model.GameState
import com.alpha.dots.ui.viewModel.GameViewModel
import com.alpha.dots.ui.viewModel.SettingsViewModel
import com.alpha.dots.util.BANNER_ID
import com.alpha.dots.util.GameStatus
import com.alpha.dots.util.SINGLE_PLAYER_GAME_OVER_SCREEN
import com.alpha.dots.util.SINGLE_PLAYER_GAME_SCREEN
import kotlin.math.sqrt

@Composable
internal fun GameScreen(
    viewModel: GameViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val gameState by viewModel.gameState.collectAsState()
    val activity = LocalContext.current as Activity

    LaunchedEffect(Unit) {
        viewModel.resetGameState()
        viewModel.startNewGame(activity)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Main content
        if (gameState?.status == GameStatus.GAME_OVER) {
            navController.navigate(SINGLE_PLAYER_GAME_OVER_SCREEN) {
                popUpTo(SINGLE_PLAYER_GAME_SCREEN) { inclusive = true }
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Black
            ) { innerPadding ->
                GameScreenContent(
                    gameState = gameState,
                    onDotClicked = { index ->
                        viewModel.onDotClicked(index, activity)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Ad banner at the bottom
        BannerAdView(
            adUnitId = BANNER_ID, // Use your test ad unit ID
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .wrapContentHeight()
        )
    }
}




@Composable
fun GameScreenContent(
    gameState: GameState?,
    onDotClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxSize()
            .background(Color.Black)
    ) {
        gameState?.let { state ->
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                Text(
                    text = "Score: ${state.score}",
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 16.dp, top = 16.dp),
                    color = Color.White,
                    fontSize = 24.sp
                )

                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.dots.isNotEmpty()) {
                        val gridSize = sqrt(state.dots.size.toFloat()).toInt()

                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (y in 0 until gridSize) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (x in 0 until gridSize) {
                                        val index = y * gridSize + x
                                        if (index < state.dots.size) {
                                            val dot = state.dots[index]
                                            Box(
                                                modifier = Modifier
                                                    .size(dot.size.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(dot.colorInt))
                                                    .clickable { onDotClicked(index) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Game Over",
                            color = Color.White,
                            fontSize = 32.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenContentPreview() {
    val sampleDots = listOf(
        Dot(x = 0f, y = 0f, size = 50f, colorInt = Color.Red.toArgb(), isTarget = true),
        Dot(x = 60f, y = 0f, size = 50f, colorInt = Color.White.toArgb(), isTarget = false),
        Dot(x = 0f, y = 60f, size = 50f, colorInt = Color.White.toArgb(), isTarget = false),
        Dot(x = 60f, y = 60f, size = 50f, colorInt = Color.White.toArgb(), isTarget = false)
    )
    val sampleGameState = GameState(
        dots = sampleDots,
        score = 0,
        round = 1,
        timeLeft = 3000L
    )

    GameScreenContent(gameState = sampleGameState, onDotClicked = {})
}