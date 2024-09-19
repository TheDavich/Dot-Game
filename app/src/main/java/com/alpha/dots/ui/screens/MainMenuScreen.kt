package com.alpha.dots.ui.screens

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.alpha.dots.ui.composables.UniversalButton
import com.alpha.dots.ui.viewModel.GameViewModel
import com.alpha.dots.ui.viewModel.LoginViewModel
import com.alpha.dots.util.MULTIPLAYER_GAME_SCREEN
import com.alpha.dots.util.SETTINGS_SCREEN
import com.alpha.dots.util.SINGLE_PLAYER_GAME_SCREEN

@Composable
fun MainMenu(
    viewModel: GameViewModel,
    navController: NavHostController,
    signInLauncher: ActivityResultLauncher<Intent>,
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    var showLoginDialog by remember { mutableStateOf(false) }
    val userId by loginViewModel.userId.collectAsState() // Observe the login state

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
                    .statusBarsPadding()
                    .clickable {
                        navController.navigate(SETTINGS_SCREEN)
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        MainMenuScreenContent(
            onSinglePlayerClicked = {
                if (userId != null) {
                    viewModel.resetGameState()
                    navController.navigate(SINGLE_PLAYER_GAME_SCREEN)
                } else {
                    showLoginDialog = true
                }
            },
            showAlertDialog = showLoginDialog,
            onDismissDialog = {
                showLoginDialog = false
            },
            onLaunchGoogleSignIn = {
                loginViewModel.signInWithGoogle(signInLauncher)
            },
            modifier = modifier.padding(innerPadding)
        )
    }
}


@Composable
fun MainMenuScreenContent(
    onSinglePlayerClicked: () -> Unit,
    showAlertDialog: Boolean,
    onDismissDialog: () -> Unit,
    onLaunchGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        UniversalButton(
            text = "Singleplayer",
            onClick = onSinglePlayerClicked
        )

        if (showAlertDialog) {
            AlertDialog(
                onDismissRequest = onDismissDialog,
                title = { Text("Sign In Required") },
                text = { Text("You need to sign in with your Google account to play the game.") },
                confirmButton = {
                    TextButton(onClick = {
                        onDismissDialog()
                        onLaunchGoogleSignIn()
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDialog) {
                        Text("Cancel")
                    }
                }
            )
        }

        UniversalButton(
            text = "Rankings",
            onClick = {
                // Handle Rankings click
            }
        )
    }
}





@Preview
@Composable
fun MainMenuPreview() {
    MainMenuScreenContent(
        onSinglePlayerClicked = { /* Handle singleplayer click */ },
        showAlertDialog = true,
        onDismissDialog = { /* Handle dismiss dialog */ },
        onLaunchGoogleSignIn = { /* Handle Google sign-in */ }
    )
}