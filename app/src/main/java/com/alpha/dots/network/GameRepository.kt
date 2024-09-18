package com.alpha.dots.network

import android.content.Context
import com.alpha.dots.model.GameData
import com.alpha.dots.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GameRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {

    private val userCollection = firebaseFirestore.collection("users")

    // Authentication methods
    suspend fun signInWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        return authResult.user?.uid ?: throw Exception("Authentication failed")
    }

    suspend fun getGooglePlayAccountId(): String {
        return firebaseAuth.currentUser?.uid ?: throw Exception("User is not authenticated")
    }

    suspend fun getUserData(userId: String): User? {
        val userDocument = userCollection.document(userId).get().await()
        return userDocument.toObject(User::class.java)
    }

    // Method to save game results in Firestore
    suspend fun saveGameResult(userId: String, gameData: GameData) {
        val userDocument = userCollection.document(userId).get().await()
        val user = userDocument.toObject(User::class.java)

        if (user != null) {
            val newTotalScore = user.totalScore + gameData.score
            val newGamesPlayed = user.gamesPlayed + 1

            // Calculate new average score
            val newAverageScore = newTotalScore / newGamesPlayed

            // Determine if this game has the new max score
            val newMaxScore = maxOf(user.maxScore, gameData.score)

            // Update average reaction time
            val newTotalReactionTime = user.avgReactionTime * user.gamesPlayed + gameData.reactionTime
            val newAvgReactionTime = newTotalReactionTime / newGamesPlayed

            // Update the user data
            val updatedUser = user.copy(
                totalScore = newTotalScore,
                avgReactionTime = newAvgReactionTime,
                gamesPlayed = newGamesPlayed,
                maxScore = newMaxScore,
                averageScore = newAverageScore
            )

            userCollection.document(userId).set(updatedUser).await()
        } else {
            // Create a new user entry if none exists
            val newUser = User(
                id = userId,
                totalScore = gameData.score,
                avgReactionTime = gameData.reactionTime,
                gamesPlayed = 1,
                maxScore = gameData.score,
                averageScore = gameData.score
            )
            userCollection.document(userId).set(newUser).await()
        }
    }

    // Method to update user's Elo score in Firestore
    suspend fun updateUserEloScore(userId: String, newEloScore: Int) {
        userCollection.document(userId).update("eloScore", newEloScore).await()
    }
}

