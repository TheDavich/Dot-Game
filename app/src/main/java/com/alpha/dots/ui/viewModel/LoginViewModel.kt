package com.alpha.dots.ui.viewModel

import android.content.Intent
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

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(application.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(application, googleSignInOptions)

    // Check if user is already signed in on app launch
    fun checkForExistingSignIn(onSignInRequired: () -> Unit, onSignedIn: () -> Unit) {
        if (isUserLoggedIn()) {
            _userId.value = firebaseAuth.currentUser?.uid
            onSignedIn()
        } else {
            onSignInRequired()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // Automatically sign in the user when launching the app for the first time
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
                    _userId.value = it.uid // Store the userId after successful sign-in
                    onSuccess(account)
                }
            } catch (e: Exception) {
                // Handle authentication errors
            }
        }
    }

    fun setUserId(userId: String) {
        _userId.value = userId
    }

    fun signOut() {
        firebaseAuth.signOut()
        _userId.value = null // Reset the userId when signed out
    }
}

