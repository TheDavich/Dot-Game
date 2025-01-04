package com.alpha.dots

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.alpha.dots.navigation.AppNavigation
import com.alpha.dots.ui.composables.SetUsernameDialog
import com.alpha.dots.ui.composables.SignInRequiredDialog
import com.alpha.dots.ui.theme.DotTheme
import com.alpha.dots.ui.viewModel.GameViewModel
import com.alpha.dots.ui.viewModel.LoginViewModel
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()
    private val gameViewModel: GameViewModel by viewModels()
    private var showLoginDialog by mutableStateOf(false)

    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ads configuration
        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf("7325F95FDE54100FCBF15749F6650AE5"))
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        MobileAds.initialize(this)


        // Set up sign-in launcher
        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            loginViewModel.handleSignInResult(
                result,
                onSuccess = { account ->
                    showLoginDialog = false
                    gameViewModel.observeUserUpdates()
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

        // Handle existing sign-in
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

                    if (showLoginDialog) {
                        SignInRequiredDialog(
                            onConfirm = { loginViewModel.signInWithGoogle(signInLauncher) },
                            onDismiss = { showLoginDialog = true }
                        )
                    }

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