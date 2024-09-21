package com.alpha.dots.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SignInRequiredDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Do nothing to make it persistent */ },
        title = { Text("Sign In Required", color = Color.White) },
        text = { Text("You need to sign in with your Google account to play the game.", color = Color.White) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = Color.Black,
    )
}
