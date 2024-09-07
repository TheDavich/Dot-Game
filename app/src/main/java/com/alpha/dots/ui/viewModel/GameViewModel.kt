package com.alpha.dots.ui.viewModel

import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.dots.model.Dot
import com.alpha.dots.model.GameData
import com.alpha.dots.model.GameState
import com.alpha.dots.model.User
import com.alpha.dots.util.GameStatus
import com.alpha.dots.model.dotsColors
import com.alpha.dots.network.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.random.Random

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState

    private val _opponentScore = MutableStateFlow(0)
    val opponentScore: StateFlow<Int> = _opponentScore

    private var currentDotSize = 43f
    private val minDotSize = 38f
    private val reactionTimes = mutableListOf<Long>()
    private var startTime: Long = 0L
    private var correctChoices = 0
    private var averageReactionTime = 0L
    private var gamesPlayed = 0

    private var timer: CountDownTimer? = null

    init {
        _gameState.value = GameState(
            dots = emptyList(),
            score = 0,
            round = 1,
            timeLeft = 0L,
            status = GameStatus.STOPPED
        )
    }

    fun startNewGame() {
        reactionTimes.clear()
        currentDotSize = 43f
        correctChoices = 0
        averageReactionTime = 0L
        _gameState.value = GameState(
            dots = initializeDots(4, currentDotSize, 4, 1),
            score = 0,
            round = 1,
            timeLeft = 3000L,
            status = GameStatus.STARTED
        )
        resetTimer()
    }

    private fun resetTimer() {
        timer?.cancel()
        val currentRound = _gameState.value?.round ?: 1
        val delayTime = if (currentRound < 10) 2000L else calculateDynamicTimeLimit()
        timer = object : CountDownTimer(delayTime, 100L) {
            override fun onTick(millisUntilFinished: Long) {}
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onFinish() {
                endGame(true) // Timer ran out, so pass true
            }
        }.start()
        startTime = System.currentTimeMillis()
    }

    private fun calculateDynamicTimeLimit(): Long {
        return if (averageReactionTime > 0) {
            averageReactionTime
        } else {
            2000L
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun onDotClicked(dotIndex: Int, context: Context) {
        val currentGameState = _gameState.value ?: return
        val clickedDot = currentGameState.dots[dotIndex]
        val reactionTime = System.currentTimeMillis() - startTime

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (currentGameState.round > 10 && reactionTime > calculateDynamicTimeLimit()) {
            endGame(true) // Timer ran out, so pass true
            return
        }

        if (clickedDot.isTarget) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE))
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
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 60, 30, 60, 30), -1))
            endGame(false) // Player clicked the wrong dot, so pass false
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
            dots = initializeDots(currentGameState.maxDots, currentDotSize, currentGameState.maxDots, newRound),
            round = newRound
        )
    }

    private fun initializeDots(maxDots: Int, dotSize: Float, currentMaxDots: Int, round: Int): List<Dot> {
        val dots = mutableListOf<Dot>()
        val gridSize = ceil(sqrt(maxDots.toDouble())).toInt()
        val totalDots = gridSize * gridSize
        val targetDotIndex = Random.nextInt(totalDots)

        if (currentMaxDots < 16) {
            for (i in 0 until totalDots) {
                val x = (i % gridSize) * (dotSize + 20)
                val y = (i / gridSize) * (dotSize + 20)

                val color = if (i == targetDotIndex) Color.Red else Color.White
                val isTarget = i == targetDotIndex
                dots.add(Dot(x, y, dotSize, color, isTarget))
            }
        } else {
            val colorIndex = round % dotsColors.size
            val baseColor = dotsColors[colorIndex]

            for (i in 0 until totalDots) {
                val x = (i % gridSize) * (dotSize + 20)
                val y = (i / gridSize) * (dotSize + 20)

                val color = if (i == targetDotIndex) adjustColor(baseColor) else baseColor
                val isTarget = i == targetDotIndex
                dots.add(Dot(x, y, dotSize, color, isTarget))
            }
        }

        return dots
    }

    private fun adjustColor(baseColor: Color): Color {
        val isBright = baseColor.red > 0.5f || baseColor.green > 0.5f || baseColor.blue > 0.5f
        return if (isBright) {
            baseColor.copy(
                red = (baseColor.red * 0.5f).coerceAtLeast(0f),
                green = (baseColor.green * 0.5f).coerceAtLeast(0f),
                blue = (baseColor.blue * 0.5f).coerceAtLeast(0f)
            )
        } else {
            baseColor.copy(
                red = (baseColor.red * 1.3f).coerceAtMost(1f),
                green = (baseColor.green * 1.3f).coerceAtMost(1f),
                blue = (baseColor.blue * 1.3f).coerceAtMost(1f)
            )
        }
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
            dots = initializeDots(nextMaxDots, currentDotSize, nextMaxDots, currentGameState.round + 1),
            round = currentGameState.round + 1,
            maxDots = nextMaxDots,
            timeLeft = (3000L / nextMaxDots.coerceAtMost(30)).coerceAtLeast(1000L)
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun endGame(timerRanOut: Boolean) {
        timer?.cancel()
        timer = null

        val currentGameState = _gameState.value ?: return
        val avgReactionTime = if (reactionTimes.isNotEmpty()) {
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

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (timerRanOut) {
            vibrator.vibrate(VibrationEffect.createOneShot(400L, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        // Save game result to Firebase and update Elo score after 10 games
        viewModelScope.launch(Dispatchers.IO) {
            saveGameResultToFirebase(currentGameState.score, avgReactionTime)
            if (gamesPlayed >= 10) {
                calculateAndUpdateEloScore()
            }
        }
    }

    private suspend fun saveGameResultToFirebase(score: Int, reactionTime: Long) {
        val userId = repository.getGooglePlayAccountId() // Assuming this method exists in your repo
        val gameData = GameData(score = score, reactionTime = reactionTime)
        repository.saveGameResult(userId, gameData)
    }

    private suspend fun calculateAndUpdateEloScore() {
        val userId = repository.getGooglePlayAccountId()
        val user = repository.getUserData(userId) ?: return

        val eloScore = calculateEloScore(user)

        repository.updateUserEloScore(userId, eloScore)
    }

    private fun calculateEloScore(user: User): Int {
        // Example formula: Elo = (totalScore / gamesPlayed) + (avgReactionTime modifier)
        val scoreBasedElo = user.totalScore / user.gamesPlayed
        val reactionModifier = (1000 / user.avgReactionTime).toInt() // Convert Long to Int
        return scoreBasedElo + reactionModifier
    }

}

