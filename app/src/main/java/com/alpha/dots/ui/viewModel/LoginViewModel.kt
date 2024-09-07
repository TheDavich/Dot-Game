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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : AndroidViewModel(application) {

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(application.getString(R.string.default_web_client_id)) // Your web client ID
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(application, googleSignInOptions)

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

                if (user != null) {
                    // Save user data to Firebase Database if needed
                    val userRef = firebaseDatabase.getReference("users").child(user.uid)
                    val newUser = User(id = user.uid)
                    userRef.setValue(newUser)

                    onSuccess(account)
                }
            } catch (e: Exception) {
                // Handle authentication errors
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
    }
}