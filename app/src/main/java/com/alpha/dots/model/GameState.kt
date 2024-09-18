package com.alpha.dots.model

import com.alpha.dots.util.GameStatus

data class GameState(
    @JvmField val dots: List<Dot> = emptyList(),
    @JvmField val score: Int = 0,
    @JvmField val round: Int = 1,
    @JvmField val timeLeft: Long = 0L,
    @JvmField val maxDots: Int = 4,
    @JvmField val avgReactionTime: Long = 0L,
    @JvmField val status: GameStatus = GameStatus.STOPPED,
    @JvmField val playerId: String = ""
)
