package com.alpha.dots.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alpha.dots.ui.viewModel.GameViewModel
import com.alpha.dots.ui.viewModel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel,
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val hapticEnabled by viewModel.hapticFeedbackEnabled.collectAsState()
    var username by remember { mutableStateOf("") }
    var isUsernameTaken by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val currentUser by gameViewModel.currentUser.collectAsState()

    // Initialize the current username if the user is available
    LaunchedEffect(currentUser) {
        currentUser?.let {
            username = it.username
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                ),
                title = { Text("Settings", color = Color.White) },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // Haptic Feedback Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Haptic Feedback",
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = { viewModel.setHapticFeedbackEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        uncheckedThumbColor = Color.Gray,
                        checkedTrackColor = Color.DarkGray,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Username Change Section
            Text(
                text = "Change Username",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // OutlinedTextField with rounded corners, black background, and white border
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    isUsernameTaken = false
                    isSuccess = false
                },
                label = { Text("Username") }, // Set hint as Username
                isError = isUsernameTaken,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)), // Rounded corners
                singleLine = true,
                shape = RoundedCornerShape(24.dp), // Rounded corner shape
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Black,
                    focusedContainerColor = Color.Black,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    cursorColor = Color.White,
                )
            )

            if (isUsernameTaken) {
                Text(
                    text = "Username is already taken!",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (isSuccess) {
                Text(
                    text = "Username updated successfully!",
                    color = Color.Green,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isLoading = true
                    gameViewModel.changeNickname(username) { isTaken ->
                        isLoading = false
                        isUsernameTaken = isTaken
                        isSuccess = !isTaken
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = username.isNotEmpty() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Update Username")
                }
            }
        }
    }
}


