package com.alpha.dots.ui.viewModel

import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.dots.model.Dot
import com.alpha.dots.model.GameData
import com.alpha.dots.model.GameState
import com.alpha.dots.model.User
import com.alpha.dots.model.dotsColors
import com.alpha.dots.network.GameRepository
import com.alpha.dots.util.GameStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.random.Random

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context,
    private val settingsViewModel: SettingsViewModel,
    private val loginViewModel: LoginViewModel,
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState

    private var currentDotSize = 43f
    private val minDotSize = 38f
    private val reactionTimes = mutableListOf<Long>()
    private var startTime: Long = 0L
    private var correctChoices = 0
    private var averageReactionTime = 0L
    private var gamesPlayed = 0

    private var timer: CountDownTimer? = null

    init {
        resetGameState()
    }

    fun resetGameState() {
        _gameState.value = GameState(
            dots = emptyList(),
            score = 0,
            round = 1,
            timeLeft = 0L,
            status = GameStatus.STOPPED
        )
        reactionTimes.clear()
        currentDotSize = 43f
        correctChoices = 0
        averageReactionTime = 0L
        startTime = 0L
        timer?.cancel()
    }

    fun startNewGame() {
        if (_gameState.value?.status == GameStatus.STOPPED) {
            resetGameState()

            _gameState.value = _gameState.value?.copy(
                dots = initializeDots(4, currentDotSize, 4, 1),
                score = 0,
                round = 1,
                timeLeft = 3000L,
                status = GameStatus.STARTED
            )
            resetTimer()
        }
    }

    private fun resetTimer() {
        timer?.cancel()
        val currentRound = _gameState.value?.round ?: 1
        val delayTime = if (currentRound < 10) 2000L else calculateDynamicTimeLimit()
        timer = object : CountDownTimer(delayTime, 100L) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                endGame(true)  // Timer ran out, handle as a loss
            }
        }.start()
        startTime = System.currentTimeMillis()
    }

    private fun calculateDynamicTimeLimit(): Long {
        return if (averageReactionTime > 0) {
            averageReactionTime + 300L
        } else {
            2000L
        }
    }

    fun onDotClicked(dotIndex: Int, context: Context) {
        val currentGameState = _gameState.value ?: return
        if (dotIndex >= currentGameState.dots.size) {
            return
        }

        val clickedDot = currentGameState.dots[dotIndex]
        val reactionTime = System.currentTimeMillis() - startTime

        // Check haptic feedback setting
        val hapticEnabled = runBlocking {
            settingsViewModel.hapticFeedbackEnabled.first()
        }

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (currentGameState.round > 10 && reactionTime > calculateDynamicTimeLimit()) {
            endGame(true)  // Timer ran out
            return
        }

        if (clickedDot.isTarget) {
            if (hapticEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else {
                    vibrator.vibrate(200)
                }
            }

            reactionTimes.add(reactionTime)
            correctChoices++

            if (currentGameState.round >= 10) {
                averageReactionTime = reactionTimes.average().toLong()
            }

            _gameState.value = currentGameState.copy(score = currentGameState.score + 1)

            if (correctChoices % 3 == 0) {
                decreaseDotSize()
                increaseDifficulty()
            } else {
                advanceRound()
            }

            resetTimer()
        } else {
            if (hapticEnabled) {
                vibrator.vibrate(longArrayOf(0, 30, 60, 30, 60, 30), -1)
            }
            handleIncorrectDotClick()  // Player clicked the wrong dot
        }
    }

    private fun handleIncorrectDotClick() {
        val currentGameState = _gameState.value ?: return

        if (currentGameState.round <= 5) {
            endGame(false, ignoreReactionTime = true)
        } else {
            endGame(false, ignoreReactionTime = false)  // Penalize for incorrect click
        }
    }

    private fun decreaseDotSize() {
        if (currentDotSize > minDotSize) {
            currentDotSize = (currentDotSize - 1.5f).coerceAtLeast(minDotSize)
        }
    }

    private fun advanceRound() {
        val currentGameState = _gameState.value ?: return
        val newRound = currentGameState.round + 1

        _gameState.value = currentGameState.copy(
            dots = initializeDots(
                currentGameState.maxDots,
                currentDotSize,
                currentGameState.maxDots,
                newRound
            ),
            round = newRound
        )
    }

    private fun initializeDots(
        maxDots: Int,
        dotSize: Float,
        currentMaxDots: Int,
        round: Int,
    ): List<Dot> {
        val dots = mutableListOf<Dot>()
        val gridSize = ceil(sqrt(maxDots.toDouble())).toInt()
        val totalDots = gridSize * gridSize
        val targetDotIndex = Random.nextInt(totalDots)

        for (i in 0 until totalDots) {
            val x = (i % gridSize) * (dotSize + 20)
            val y = (i / gridSize) * (dotSize + 20)

            val colorInt: Int
            if (maxDots < 16) {
                colorInt = if (i == targetDotIndex) {
                    Color.Red.toArgb()
                } else {
                    Color.White.toArgb()
                }
            } else {
                val baseColorIndex = round % dotsColors.size
                val baseColor = dotsColors[baseColorIndex]

                colorInt = if (i == targetDotIndex) {
                    darkenColor(baseColor, correctChoices).toArgb()
                } else {
                    baseColor.toArgb()
                }
            }

            val isTarget = i == targetDotIndex
            dots.add(Dot(x, y, dotSize, colorInt, isTarget))
        }

        return dots
    }

    private fun darkenColor(
        color: Color,
        correctChoices: Int,
        initialFactor: Float = 0.2f,
        increment: Float = 0.03f,
        maxFactor: Float = 0.8f,
    ): Color {
        val factor = (initialFactor + correctChoices * increment).coerceAtMost(maxFactor)

        return Color(
            red = (color.red * factor).coerceIn(0f, 1f),
            green = (color.green * factor).coerceIn(0f, 1f),
            blue = (color.blue * factor).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }

    private fun increaseDifficulty() {
        val currentGameState = _gameState.value ?: return
        val nextMaxDots = when (currentGameState.maxDots) {
            4 -> 9
            9 -> 16
            16 -> 25
            25 -> 36
            else -> 36
        }

        _gameState.value = currentGameState.copy(
            dots = initializeDots(
                nextMaxDots,
                currentDotSize,
                nextMaxDots,
                currentGameState.round + 1
            ),
            round = currentGameState.round + 1,
            maxDots = nextMaxDots,
            timeLeft = (3000L / nextMaxDots.coerceAtMost(30)).coerceAtLeast(1000L)
        )
    }

    fun endGame(timerRanOut: Boolean, ignoreReactionTime: Boolean = false) {
        timer?.cancel()
        timer = null

        val currentGameState = _gameState.value ?: return

        val avgReactionTime = if (!ignoreReactionTime && reactionTimes.isNotEmpty()) {
            reactionTimes.average().toLong()
        } else {
            0L
        }

        gamesPlayed += 1

        _gameState.value = currentGameState.copy(
            dots = emptyList(),
            score = currentGameState.score,
            round = 0,
            timeLeft = 0L,
            avgReactionTime = avgReactionTime,
            status = GameStatus.GAME_OVER
        )

        // Haptic feedback when the timer runs out
        val hapticEnabled = runBlocking {
            settingsViewModel.hapticFeedbackEnabled.first()
        }
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (hapticEnabled && timerRanOut) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        400L,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(400)
            }
        }

        // Save the game result and calculate the Elo score only after round 5
        viewModelScope.launch(Dispatchers.IO) {
            if (currentGameState.round > 5) {
                try {
                    val userId = loginViewModel.userId.value ?: return@launch
                    val gameData = GameData(
                        score = currentGameState.score,
                        reactionTime = avgReactionTime
                    )

                    // Save game results in Firestore
                    repository.saveGameResult(userId, gameData)

                    // Fetch user data from Firestore
                    val user = repository.getUserData(userId) ?: return@launch

                    // Apply Elo calculation or penalty
                    val newEloScore = calculateEloScoreWithPenalty(user, currentGameState.score, avgReactionTime, currentGameState.round, timerRanOut)
                    repository.updateUserEloScore(userId, newEloScore)
                } catch (e: Exception) {
                    Log.e("GameViewModel", "Error saving game result or updating Elo score: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun calculateEloScoreWithPenalty(
        user: User,
        lastGameScore: Int,
        lastGameReactionTime: Long,
        roundsPlayed: Int,
        timerRanOut: Boolean
    ): Int {
        var newEloScore = user.eloScore

        // Penalize if the score is lower than average or if time ran out
        if (lastGameScore < user.averageScore || timerRanOut) {
            val scoreDeficit = user.averageScore - lastGameScore
            val deficitPercentage = scoreDeficit.toFloat() / user.averageScore
            val scorePenalty = (50 + (deficitPercentage * 150)).toInt()
            newEloScore -= scorePenalty
        }

        // Penalize if the reaction time is worse than the average and rounds played are more than user's games played
        if (lastGameReactionTime > user.avgReactionTime && roundsPlayed > user.gamesPlayed) {
            val reactionTimeDeficit = lastGameReactionTime - user.avgReactionTime
            val reactionTimePenaltyFactor = reactionTimeDeficit.toFloat() / user.avgReactionTime
            val reactionTimePenalty = (50 + (reactionTimePenaltyFactor * 150)).toInt()
            newEloScore -= reactionTimePenalty
        }

        // Reward if both score and reaction time improve
        if (lastGameScore > user.averageScore && lastGameReactionTime < user.avgReactionTime) {
            val scoreImprovementFactor = (lastGameScore.toFloat() / user.averageScore).coerceAtLeast(1.0f)
            val reactionTimeImprovementFactor = (user.avgReactionTime.toDouble() / lastGameReactionTime).coerceAtLeast(1.0)
            val performanceFactor = (reactionTimeImprovementFactor * 0.6) + (scoreImprovementFactor * 0.4)
            val performanceBoost = ((performanceFactor - 1.0) * 100).toInt()
            newEloScore += performanceBoost
        }

        return newEloScore.coerceAtLeast(0)
    }
}

