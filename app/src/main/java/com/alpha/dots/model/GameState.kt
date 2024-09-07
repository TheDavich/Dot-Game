package com.alpha.dots.model

import com.alpha.dots.util.GameStatus

data class GameState(
    val dots: List<Dot>,
    val score: Int,
    val round: Int,
    val timeLeft: Long,
    val maxDots: Int = 4,
    val avgReactionTime: Long = 0,
    val status: GameStatus = GameStatus.STOPPED // Default to STOPPED
)
