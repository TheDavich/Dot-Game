package com.alpha.dots.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Dot(
    var x: Float = 0f,
    var y: Float = 0f,
    var size: Float = 0f,
    var colorInt: Int = Color.White.toArgb(),
    var isTarget: Boolean = false
)
