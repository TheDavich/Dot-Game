package com.alpha.dots

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.alpha.dots.navigation.AppNavigation
import com.alpha.dots.ui.composables.SetUsernameDialog
import com.alpha.dots.ui.composables.SignInRequiredDialog
import com.alpha.dots.ui.theme.DotTheme
import com.alpha.dots.ui.viewModel.GameViewModel
import com.alpha.dots.ui.viewModel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()
    private val gameViewModel: GameViewModel by viewModels()

    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private var showLoginDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the launcher for Google Sign-In
        signInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                loginViewModel.handleSignInResult(
                    result,
                    onSuccess = { account ->
                        showLoginDialog = false
                        gameViewModel.observeUserUpdates() // Ensure GameViewModel listens to user updates
                    },
                    onError = { e ->
                        Toast.makeText(
                            this,
                            "Sign-in failed: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                        showLoginDialog = true
                    }
                )
            }

        loginViewModel.checkForExistingSignIn(
            onSignInRequired = {
                showLoginDialog = true
                loginViewModel.signInWithGoogle(signInLauncher)
            },
            onSignedIn = {
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                gameViewModel.observeUserUpdates()
            }
        )

        setContent {
            DotTheme {
                val currentUser by gameViewModel.currentUser.collectAsState()
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black
                ) { innerPadding ->

                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        signInLauncher = signInLauncher
                    )


                    // Sign-in dialog
                    if (showLoginDialog) {
                        SignInRequiredDialog(
                            onConfirm = { loginViewModel.signInWithGoogle(signInLauncher) },
                            onDismiss = { showLoginDialog = true }
                        )
                    }

                    // Username dialog
                    if (currentUser != null && currentUser!!.username.isEmpty()) {
                        SetUsernameDialog(gameViewModel = gameViewModel) {
                            // Dismiss the dialog once username is set
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DotTheme {

    }
}