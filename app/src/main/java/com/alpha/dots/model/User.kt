package com.alpha.dots.model

data class User(
    val id: String = "",
    val totalScore: Int = 0,
    val avgReactionTime: Long = 0L,
    val gamesPlayed: Int = 0,
    val eloScore: Int = 1000 // Starting Elo score
)