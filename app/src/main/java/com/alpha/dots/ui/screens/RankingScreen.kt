package com.alpha.dots.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alpha.dots.model.User
import com.alpha.dots.ui.viewModel.RankingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RankingScreen(
    rankingViewModel: RankingViewModel,
    modifier: Modifier = Modifier
) {
    val usersRanking by rankingViewModel.usersRanking.collectAsState()
    val currentUser by rankingViewModel.currentUser.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Rankings", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            currentUser?.let { user ->
                // Display current user information at the top
                CurrentUserHeader(user = user)
                Spacer(modifier = Modifier.height(24.dp)) // Add spacing between header and rankings list
            }

            if (usersRanking.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pass the index (ranking position) along with user data
                    itemsIndexed(usersRanking.sortedByDescending { it.eloScore }) { index, user ->
                        RankingItem(rank = index + 1, user = user)
                    }
                }
            } else {
                Text(
                    text = "No rankings available",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun CurrentUserHeader(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(text = "Username: ${user.username}", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        Text(text = "Elo Score: ${user.eloScore}", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        Text(text = "Games Played: ${user.gamesPlayed}", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        Text(text = "Average Score: ${user.averageScore}", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        Text(text = "Average Reaction Time: ${user.avgReactionTime} ms", color = Color.White, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun RankingItem(rank: Int, user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Display the rank in #format and username on the left, and the Elo score on the right
        Text(
            text = "#$rank ${user.username}",
            color = Color.Black,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f) // Ensures that this text takes available space
        )
        Text(
            text = "${user.eloScore}",
            color = Color.Black,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}





