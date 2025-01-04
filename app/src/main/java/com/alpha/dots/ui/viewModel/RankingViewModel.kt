package com.alpha.dots.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.dots.model.User
import com.alpha.dots.network.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class RankingViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _usersRanking = MutableStateFlow<List<User>>(emptyList())
    val usersRanking: StateFlow<List<User>> = _usersRanking

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        listenForRankingsUpdates()
    }

    /**
     * This function listens for changes in the Firestore database in real time.
     * It will update the ranking list whenever there's a change in the database.
     */
    private fun listenForRankingsUpdates() {
        repository.listenToUsersCollection { users ->
            // Sort users by Elo score in descending order
            val sortedUsers = users.sortedByDescending { it.eloScore }
            _usersRanking.value = sortedUsers

            // Update the current user data
            viewModelScope.launch {
                val currentUserId = repository.getGooglePlayAccountId()
                _currentUser.value = sortedUsers.find { it.id == currentUserId }
            }
        }
    }
}

