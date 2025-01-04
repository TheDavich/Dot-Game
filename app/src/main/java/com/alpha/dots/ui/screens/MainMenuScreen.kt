package com.alpha.dots.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.alpha.dots.ui.composables.SetUsernameDialog
import com.alpha.dots.ui.composables.UniversalButton
import com.alpha.dots.ui.viewModel.GameViewModel
import com.alpha.dots.ui.viewModel.LoginViewModel
import com.alpha.dots.util.BANNER_ID
import com.alpha.dots.util.RANKING_SCREEN
import com.alpha.dots.util.SETTINGS_SCREEN
import com.alpha.dots.util.SINGLE_PLAYER_GAME_SCREEN
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
internal fun MainMenu(
    viewModel: GameViewModel,
    navController: NavHostController,
    signInLauncher: ActivityResultLauncher<Intent>,
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = hiltViewModel(),
) {
    var showLoginDialog by remember { mutableStateOf(false) }
    var showUsernameDialog by remember { mutableStateOf(false) }
    val userId by loginViewModel.userId.collectAsState(initial = null)
    val currentUser by viewModel.currentUser.collectAsState()

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
                if (currentUser?.username.isNullOrEmpty()) {
                    // Show dialog to set username if it's empty
                    showUsernameDialog = true
                } else if (userId != null) {
                    viewModel.resetGameState()
                    navController.navigate(SINGLE_PLAYER_GAME_SCREEN)
                } else {
                    showLoginDialog = true
                }
            },
            showAlertDialog = showLoginDialog,
            onDismissDialog = { showLoginDialog = false },
            onLaunchGoogleSignIn = { loginViewModel.signInWithGoogle(signInLauncher) },
            modifier = modifier.padding(innerPadding),
            onRankingClicked = { navController.navigate(RANKING_SCREEN) },
            showUsernameDialog = showUsernameDialog,
            onDismissUsernameDialog = { showUsernameDialog = false },  // Properly dismiss the dialog
            gameViewModel = viewModel
        )
    }
}

@Composable
internal fun MainMenuScreenContent(
    onSinglePlayerClicked: () -> Unit,
    showAlertDialog: Boolean,
    onDismissDialog: () -> Unit,
    onLaunchGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    onRankingClicked: () -> Unit,
    showUsernameDialog: Boolean,
    onDismissUsernameDialog: () -> Unit,
    gameViewModel: GameViewModel,
) {
    val context = LocalContext.current
    val activity = context as? Activity  // Cast context to Activity to pass to GameViewModel function

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            UniversalButton(
                text = "Singleplayer",
                onClick = onSinglePlayerClicked
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            UniversalButton(
                text = "Rankings",
                onClick = onRankingClicked
            )

            if (showUsernameDialog) {
                SetUsernameDialog(
                    gameViewModel = gameViewModel,
                    onDismissRequest = onDismissUsernameDialog
                )
            }
        }

        BannerAdView(
            adUnitId = BANNER_ID,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .wrapContentHeight()
        )
    }
}




@Composable
fun BannerAdView(adUnitId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}





@Preview
@Composable
fun MainMenuPreview() {
    MainMenuScreenContent(
        onSinglePlayerClicked = { /* Handle singleplayer click */ },
        showAlertDialog = true,
        onDismissDialog = { /* Handle dismiss dialog */ },
        onLaunchGoogleSignIn = { /* Handle Google sign-in */ },
        onRankingClicked = {},
        showUsernameDialog = true,
        onDismissUsernameDialog = { /* Handle dismiss username dialog */ },
        gameViewModel = hiltViewModel()
    )
}