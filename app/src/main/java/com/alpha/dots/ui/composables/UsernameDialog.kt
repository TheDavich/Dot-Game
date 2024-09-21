package com.alpha.dots.ui.composables

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alpha.dots.ui.viewModel.GameViewModel

@Composable
fun SetUsernameDialog(
    gameViewModel: GameViewModel,
    onDismissRequest: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var isUsernameTaken by remember { mutableStateOf(false) }
    val currentUser by gameViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(currentUser) {
        Log.d("SetUsernameDialog", "Current user loaded: ${currentUser?.username}")
        username = currentUser?.username ?: ""
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Set Your Username", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        Log.d("SetUsernameDialog", "Username changed to: $it")
                        username = it
                        isUsernameTaken = false
                    },
                    label = { Text("Username", color = Color.White) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Black,
                        focusedContainerColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        cursorColor = Color.White,
                    ),
                    shape = RoundedCornerShape(24.dp),
                    isError = isUsernameTaken,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (isUsernameTaken) {
                    Text(text = "Username is already taken!", color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Log.d("SetUsernameDialog", "Confirm button clicked with username: $username")
                    gameViewModel.changeNickname(username) { isTaken ->
                        if (!isTaken) {
                            Log.d("SetUsernameDialog", "Username set successfully: $username")
                            Toast.makeText(context, "Username set successfully!", Toast.LENGTH_SHORT).show()
                            onDismissRequest()
                        } else {
                            Log.d("SetUsernameDialog", "Username is already taken: $username")
                            isUsernameTaken = true
                        }
                    }
                },
                enabled = username.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Confirm")
            }
        },
        containerColor = Color.Black
    )
}




