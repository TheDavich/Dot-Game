package com.alpha.dots.model

import androidx.compose.ui.graphics.Color

data class Dot(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val isTarget: Boolean
)
