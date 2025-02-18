package com.alpha.dots.navigation

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alpha.dots.ui.screens.GameOverScreen
import com.alpha.dots.ui.screens.GameScreen
import com.alpha.dots.ui.screens.MainMenu
import com.alpha.dots.ui.screens.RankingScreen
import com.alpha.dots.ui.screens.SettingsScreen
import com.alpha.dots.ui.viewModel.GameViewModel
import com.alpha.dots.ui.viewModel.LoginViewModel
import com.alpha.dots.ui.viewModel.RankingViewModel
import com.alpha.dots.ui.viewModel.SettingsViewModel
import com.alpha.dots.util.MAIN_MENU_SCREEN
import com.alpha.dots.util.RANKING_SCREEN
import com.alpha.dots.util.SETTINGS_SCREEN
import com.alpha.dots.util.SINGLE_PLAYER_GAME_OVER_SCREEN
import com.alpha.dots.util.SINGLE_PLAYER_GAME_SCREEN

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    signInLauncher: ActivityResultLauncher<Intent>
) {
    val viewModel: GameViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val rankingViewModel: RankingViewModel = hiltViewModel()
    NavHost(
        navController = navController,
        startDestination = MAIN_MENU_SCREEN,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable(
            route = MAIN_MENU_SCREEN,
            enterTransition = { EnterTransition.None} ,
            exitTransition = {ExitTransition.None} ,
        ) {
            MainMenu(
                viewModel = viewModel,
                navController = navController,
                loginViewModel = loginViewModel,
                signInLauncher = signInLauncher // Pass the launcher to MainMenu
            )
        }
        composable(
            route = SINGLE_PLAYER_GAME_SCREEN,
            enterTransition = {EnterTransition.None} ,
            exitTransition = {ExitTransition.None} ,
        ) {
            GameScreen(
                viewModel = viewModel,
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
        composable(
            route = SINGLE_PLAYER_GAME_OVER_SCREEN,
            enterTransition = {EnterTransition.None} ,
            exitTransition = {ExitTransition.None} ,
        ) {
            GameOverScreen(
                viewModel = viewModel,
                navController = navController,
            )
        }
        composable(
            route = SETTINGS_SCREEN,
            enterTransition = {EnterTransition.None} ,
            exitTransition = {ExitTransition.None} ,
        ) {
            SettingsScreen(
                viewModel = settingsViewModel,
                gameViewModel = viewModel
            )
        }
        composable(
            route = RANKING_SCREEN,
            enterTransition = {EnterTransition.None} ,
            exitTransition = {ExitTransition.None} ,
        ) {
            RankingScreen(
                rankingViewModel = rankingViewModel
            )
        }
    }
}