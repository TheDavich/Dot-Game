package com.alpha.dots.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpha.dots.ui.screens.GameOverScreen
import com.alpha.dots.ui.screens.GameScreen
import com.alpha.dots.ui.screens.MainMenu
import com.alpha.dots.ui.viewModel.GameViewModel
import com.alpha.dots.util.MainMenuScreen
import com.alpha.dots.util.SinglePlayerGameOverScreen
import com.alpha.dots.util.SinglePlayerGameScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val viewModel: GameViewModel = hiltViewModel()
    NavHost(
        navController = navController,
        startDestination = MainMenuScreen,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable(
            route = MainMenuScreen,
            enterTransition = { EnterTransition.None} ,
            exitTransition = {ExitTransition.None} ,
        ) {
            MainMenu(
                viewModel = viewModel,
                navController = navController,
            )
        }
        composable(
            route = SinglePlayerGameScreen,
            enterTransition = {EnterTransition.None} ,
            exitTransition = {ExitTransition.None} ,
        ) {
            GameScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(
            route = SinglePlayerGameOverScreen,
            enterTransition = {EnterTransition.None} ,
            exitTransition = {ExitTransition.None} ,
        ) {
            GameOverScreen(
                viewModel = viewModel,
                navController = navController,
            )
        }
    }
}