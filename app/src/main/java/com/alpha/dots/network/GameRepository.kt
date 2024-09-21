package com.alpha.dots.network

import android.content.Context
import android.util.Log
import com.alpha.dots.model.GameData
import com.alpha.dots.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GameRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context,
) {

    private val userCollection = firebaseFirestore.collection("users")

    // Method to get the current user ID
    suspend fun getGooglePlayAccountId(): String? {
        return firebaseAuth.currentUser?.uid
    }


    suspend fun getAllUsers(): List<User> {
        val querySnapshot = userCollection.get().await()
        return querySnapshot.documents.mapNotNull { it.toObject(User::class.java) }
    }

    fun listenToUsersCollection(onUsersUpdate: (List<User>) -> Unit) {
        userCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("GameRepository", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                onUsersUpdate(users)
            }
        }
    }

    // Method to save game results in Firestore
    suspend fun saveGameResult(userId: String, gameData: GameData) {
        val userDocument = userCollection.document(userId).get().await()
        val user = userDocument.toObject(User::class.java)

        if (user != null) {
            val newTotalScore = user.totalScore + gameData.score
            val newGamesPlayed = user.gamesPlayed + 1
            val newAverageScore = newTotalScore / newGamesPlayed
            val newMaxScore = maxOf(user.maxScore, gameData.score)

            // Update average reaction time
            val newTotalReactionTime =
                user.avgReactionTime * user.gamesPlayed + gameData.reactionTime
            val newAvgReactionTime = newTotalReactionTime / newGamesPlayed

            // Update the user data
            val updatedUser = user.copy(
                totalScore = newTotalScore,
                avgReactionTime = newAvgReactionTime,
                gamesPlayed = newGamesPlayed,
                maxScore = newMaxScore,
                averageScore = newAverageScore
            )

            // Save the updated user data back to Firestore
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

    // Method to update Elo score in Firestore
    suspend fun updateUserEloScore(userId: String, newEloScore: Int) {
        userCollection.document(userId).update("eloScore", newEloScore).await()
    }


    suspend fun getUserData(userId: String): User? {
        return try {
            val userDocument = userCollection.document(userId).get().await()
            userDocument.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("GameRepository", "Failed to fetch user data: ${e.localizedMessage}")
            null
        }
    }

    suspend fun isNicknameTaken(nickname: String): Boolean {
        return try {
            val querySnapshot = userCollection.whereEqualTo("username", nickname).get().await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            Log.e("GameRepository", "Failed to check nickname: ${e.localizedMessage}")
            false // Default to nickname not being taken to avoid blocking progress
        }
    }

    suspend fun updateUserUsername(userId: String, newUsername: String) {
        try {
            userCollection.document(userId).update("username", newUsername).await()
            Log.d("GameRepository", "Username updated in Firestore for user: $userId")
        } catch (e: Exception) {
            Log.e("GameRepository", "Failed to update username: ${e.localizedMessage}")
        }
    }
}




