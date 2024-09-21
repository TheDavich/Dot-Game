package com.alpha.dots.ui.viewModel

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.dots.Application
import com.alpha.dots.R
import com.alpha.dots.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) : AndroidViewModel(application) {

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(application.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(application, googleSignInOptions)

    init {
        checkForExistingSignIn() // Check for existing sign-in immediately on app start
    }

    fun checkForExistingSignIn(onSignInRequired: (() -> Unit)? = null, onSignedIn: (() -> Unit)? = null) {
        if (isUserLoggedIn()) {
            _userId.value = firebaseAuth.currentUser?.uid
            _userId.value?.let { loadCurrentUser(it) }
            onSignedIn?.invoke()
        } else {
            onSignInRequired?.invoke()
        }
    }

    private fun loadCurrentUser(userId: String) {
        // Listen for real-time updates on the user's data
        val userRef = firebaseFirestore.collection("users").document(userId)
        userRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("LoginViewModel", "Failed to listen to user data", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                _currentUser.value = snapshot.toObject(User::class.java)
                Log.d("LoginViewModel", "User loaded: ${_currentUser.value?.username}")
            }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // Sign in the user with Google
    fun signInWithGoogle(launcher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    fun handleSignInResult(result: ActivityResult, onSuccess: (GoogleSignInAccount) -> Unit, onError: (Exception) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account, onSuccess)
        } catch (e: ApiException) {
            onError(e)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount, onSuccess: (GoogleSignInAccount) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        viewModelScope.launch {
            try {
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val user = authResult.user
                user?.let {
                    _userId.value = it.uid
                    createFirestoreUserIfNotExists(it.uid)
                    loadCurrentUser(it.uid)
                    onSuccess(account)
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error signing in with Google: ${e.localizedMessage}")
            }
        }
    }

    private fun createFirestoreUserIfNotExists(userId: String) {
        val userRef = firebaseFirestore.collection("users").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val newUser = User(id = userId, username = "", totalScore = 0)
                userRef.set(newUser)
                Log.d("LoginViewModel", "New user created in Firestore")
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _userId.value = null
    }
}




