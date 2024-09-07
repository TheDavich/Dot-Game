package com.alpha.dots.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.alpha.dots.model.Dot
import com.alpha.dots.model.GameState
import com.alpha.dots.util.GameStatus
import com.alpha.dots.ui.viewModel.GameViewModel
import com.alpha.dots.util.SinglePlayerGameOverScreen
import kotlin.math.sqrt

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val context = LocalContext.current

    BackHandler {}

    LaunchedEffect(Unit) {
        viewModel.startNewGame()
    }

    if (gameState?.status == GameStatus.GAME_OVER) {
        navController.navigate(SinglePlayerGameOverScreen)
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Black
        ) { innerPadding ->
            GameScreenContent(
                gameState = gameState,
                onDotClicked = { index ->
                    viewModel.onDotClicked(index, context)
                },
                modifier = modifier.padding(innerPadding)
            )
        }
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
                                                .background(dot.color)
                                                .clickable { onDotClicked(index) }
                                        )
                                    }
                                }
                            }
                        }
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
        Dot(x = 0f, y = 0f, size = 50f, color = Color.Red, isTarget = true),
        Dot(x = 60f, y = 0f, size = 50f, color = Color.White, isTarget = false),
        Dot(x = 0f, y = 60f, size = 50f, color = Color.White, isTarget = false),
        Dot(x = 60f, y = 60f, size = 50f, color = Color.White, isTarget = false)
    )
    val sampleGameState = GameState(
        dots = sampleDots,
        score = 0,
        round = 1,
        timeLeft = 3000L
    )

    GameScreenContent(gameState = sampleGameState, onDotClicked = {})
}