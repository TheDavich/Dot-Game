package com.alpha.dots.ui.viewModel

import android.app.Activity
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
import com.alpha.dots.util.GAME_OVER_AD_ID
import com.alpha.dots.util.GameStatus
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.lifecycle.HiltViewModel
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
internal class GameViewModel @Inject constructor(
    private val repository: GameRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
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

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        observeSignIn()
        observeUserUpdates()
        resetGameState()
    }

    private var interstitialAd: InterstitialAd? = null
    private var gamesLost = 0

    // Load an Interstitial ad
    fun loadInterstitialAd(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd
            .load(activity, GAME_OVER_AD_ID, adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        Log.d("GameViewModel", "Interstitial ad successfully loaded.")
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e("GameViewModel", "Interstitial ad failed to load: ${adError.message}, Code: ${adError.code}, Domain: ${adError.domain}")
                        interstitialAd = null
                    }
                }
            )
    }




    private fun showInterstitialAd(activity: Activity) {
        if (interstitialAd != null) {
            Log.d("GameViewModel", "Showing interstitial ad.")
            interstitialAd?.show(activity)

            // Immediately set to null after showing to avoid trying to show the same ad again
            interstitialAd = null

            // Reload ad after showing
            loadInterstitialAd(activity)
        } else {
            Log.e("GameViewModel", "Interstitial ad not loaded. Attempting to reload.")
            loadInterstitialAd(activity)
        }
    }




    fun incrementGamesLost(activity: Activity) {
        gamesLost++
        Log.d("GameViewModel", "Games lost incremented to: $gamesLost")

        if (gamesLost >= 3) {
            Log.d("GameViewModel", "3 games lost. Attempting to show interstitial ad.")
            showInterstitialAd(activity)
            gamesLost = 0 // Reset counter after showing the ad
        }
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

    fun startNewGame(activity: Activity) {
        if (_gameState.value?.status == GameStatus.STOPPED) {
            resetGameState()

            _gameState.value = _gameState.value?.copy(
                dots = initializeDots(4, currentDotSize, 4, 1),
                score = 0,
                round = 1,
                timeLeft = 3000L,
                status = GameStatus.STARTED
            )
            resetTimer(activity)

            // Load the interstitial ad at the start of each new game
            loadInterstitialAd(activity)
        }
    }



    private fun observeSignIn() {
        viewModelScope.launch {
            loginViewModel.userId.collect { userId ->
                if (userId != null) {
                    loadCurrentUser(userId) // Ensure user is loaded after sign-in
                }
            }
        }
    }

    fun observeUserUpdates() {
        viewModelScope.launch {
            loginViewModel.currentUser.collect { user ->
                if (user != null) {
                    Log.d("GameViewModel", "User data received: ${user.username}")
                    _currentUser.value = user
                }
            }
        }
    }

    private fun resetTimer(activity: Activity) {
        timer?.cancel()
        val currentRound = _gameState.value?.round ?: 1
        val delayTime = if (currentRound < 10) 2000L else calculateDynamicTimeLimit()
        timer = object : CountDownTimer(delayTime, 100L) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                endGame(true, activity)  // Pass activity to endGame when timer runs out
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

    fun onDotClicked(dotIndex: Int, activity: Activity) {
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

        val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (currentGameState.round > 10 && reactionTime > calculateDynamicTimeLimit()) {
            endGame(true, activity)  // Timer ran out
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

            resetTimer(activity)  // Pass activity here
        } else {
            if (hapticEnabled) {
                vibrator.vibrate(longArrayOf(0, 30, 60, 30, 60, 30), -1)
            }
            handleIncorrectDotClick(activity)  // Pass activity to handleIncorrectDotClick
        }
    }


    private fun handleIncorrectDotClick(activity: Activity) {
        val currentGameState = _gameState.value ?: return

        // Mark the game as lost and handle it accordingly
        if (currentGameState.round <= 5) {
            endGame(false, activity, ignoreReactionTime = true)
        } else {
            endGame(false, activity, ignoreReactionTime = false)  // Penalize for incorrect click
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

    fun endGame(timerRanOut: Boolean, activity: Activity, ignoreReactionTime: Boolean = false) {
        timer?.cancel()
        timer = null

        Log.d("GameViewModel", "Ending game. Timer ran out: $timerRanOut, Ignore reaction time: $ignoreReactionTime")


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
        val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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
                    val newEloScore = calculateEloScoreWithPenalty(
                        user,
                        currentGameState.score,
                        avgReactionTime,
                        currentGameState.round,
                        timerRanOut
                    )
                    repository.updateUserEloScore(userId, newEloScore)
                } catch (e: Exception) {
                    Log.e("GameViewModel", "Error saving game result or updating Elo score: ${e.localizedMessage}")
                }
            }
        }

        // Increment lost games if the timer ran out or user clicked the wrong dot
        if (timerRanOut || !ignoreReactionTime) {
            Log.d("GameViewModel", "Incrementing lost games due to end of game condition.")
            incrementGamesLost(activity)
        }
    }


    fun loadCurrentUser(userId: String) {
        viewModelScope.launch {
            try {
                _currentUser.value = repository.getUserData(userId)
                Log.d("GameViewModel", "User loaded: ${_currentUser.value?.username}")
            } catch (e: Exception) {
                Log.e("GameViewModel", "Failed to load user: ${e.localizedMessage}")
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

        // **Penalty 1: Severe time-out or extremely poor performance**
        // If the score is significantly below average or the timer ran out.
        if (lastGameScore < user.averageScore * 0.6 || timerRanOut) {
            val scoreDeficit = user.averageScore - lastGameScore
            val deficitPercentage = (scoreDeficit.toFloat() / user.averageScore).coerceAtMost(1.0f)
            val timePenalty = (deficitPercentage * 100).toInt()  // Moderate penalty based on score deficit
            newEloScore -= timePenalty
        }

        // **Penalty 2: Extreme inconsistency between reaction time and score**
        // Prevent reaction time abuse: faster-than-usual reaction time but poor score.
        if (lastGameReactionTime < user.avgReactionTime * 0.8 && lastGameScore < user.averageScore * 0.7) {
            val reactionFactor = (user.avgReactionTime.toDouble() / lastGameReactionTime).coerceAtLeast(1.0)
            val abusePenalty = ((reactionFactor - 1.0) * 100).toInt()  // Smaller abuse penalty than before
            newEloScore -= abusePenalty
        }

        // **Reward 1: Consistency and significant improvement**
        // If the player improves both score and reaction time beyond their averages.
        if (lastGameScore > user.averageScore && lastGameReactionTime < user.avgReactionTime) {
            val scoreImprovementFactor = (lastGameScore.toFloat() / user.averageScore).coerceAtLeast(1.0f)
            val reactionTimeImprovementFactor = (user.avgReactionTime.toDouble() / lastGameReactionTime).coerceAtLeast(1.0)
            val improvementFactor = (reactionTimeImprovementFactor * 0.6) + (scoreImprovementFactor * 0.4)  // 60% reaction, 40% score
            val performanceBoost = ((improvementFactor - 1.0) * 150).toInt()  // Slightly larger performance boost
            newEloScore += performanceBoost
        }

        // **Reward 2: Small improvements should be rewarded**
        // Give slight increases even if the player shows minor improvements in one area.
        else if (lastGameScore > user.averageScore * 1.1 || lastGameReactionTime < user.avgReactionTime * 0.9) {
            val slightImprovementBoost = (20..50).random()  // A smaller random boost to keep it engaging
            newEloScore += slightImprovementBoost
        }

        // **Penalty 3: Excessive reaction time for high scores**
        // Penalize if the player’s reaction time is much worse than their average but their score is higher.
        if (lastGameScore > user.averageScore && lastGameReactionTime > user.avgReactionTime * 1.3) {
            val reactionTimeDeficit = lastGameReactionTime - user.avgReactionTime
            val reactionPenalty = ((reactionTimeDeficit.toFloat() / user.avgReactionTime) * 70).toInt()
            newEloScore -= reactionPenalty
        }

        // **Reward 3: Consistent improvement across games**
        // Players who perform consistently above average should gain more Elo over time.
        if (lastGameScore >= user.averageScore * 1.2 && lastGameReactionTime <= user.avgReactionTime * 0.9) {
            val consistencyBonus = 40 + (lastGameScore - user.averageScore) / 10  // Scales with performance
            newEloScore += consistencyBonus
        }

        // Ensure the Elo score does not drop below 0.
        return newEloScore.coerceAtLeast(0)
    }


    fun changeNickname(newNickname: String, onNicknameTaken: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isTaken = repository.isNicknameTaken(newNickname)
            if (!isTaken) {
                val user = _currentUser.value ?: return@launch
                repository.updateUserUsername(user.id, newNickname)
                _currentUser.value = user.copy(username = newNickname) // Update user with new username
                onNicknameTaken(false) // Success
            } else {
                onNicknameTaken(true) // Username is taken
            }
        }
    }
}