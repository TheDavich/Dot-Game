package com.alpha.dots.network

import android.content.Context
import com.alpha.dots.model.GameData
import com.alpha.dots.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GameRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth, // Inject FirebaseAuth
    @ApplicationContext private val context: Context
) {

    private val userRef = firebaseDatabase.getReference("users")

    suspend fun signInWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        return authResult.user?.uid ?: throw Exception("Authentication failed")
    }

    suspend fun getGooglePlayAccountId(): String {
        return firebaseAuth.currentUser?.uid ?: throw Exception("User is not authenticated")
    }

    suspend fun getUserData(userId: String): User? {
        val userSnapshot = userRef.child(userId).get().await()
        return userSnapshot.getValue(User::class.java)
    }

    suspend fun saveGameResult(userId: String, gameData: GameData) {
        val gameId = userRef.child(userId).child("games").push().key ?: return
        userRef.child(userId).child("games").child(gameId).setValue(gameData)

        // Update user's total score and reaction time
        val userSnapshot = userRef.child(userId).get().await()
        val user = userSnapshot.getValue(User::class.java) ?: return

        val newTotalScore = user.totalScore + gameData.score
        val newTotalReactionTime = user.avgReactionTime * user.gamesPlayed + gameData.reactionTime
        val newGamesPlayed = user.gamesPlayed + 1

        val updatedUser = user.copy(
            totalScore = newTotalScore,
            avgReactionTime = newTotalReactionTime / newGamesPlayed,
            gamesPlayed = newGamesPlayed
        )

        userRef.child(userId).setValue(updatedUser)
    }

    suspend fun updateUserEloScore(userId: String, newEloScore: Int) {
        userRef.child(userId).child("eloScore").setValue(newEloScore)
    }
}
