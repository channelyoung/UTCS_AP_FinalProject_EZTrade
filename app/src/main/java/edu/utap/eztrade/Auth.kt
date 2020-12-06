package edu.utap.eztrade

import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class Auth(activity: MainActivity) {
    companion object {
        const val rcSignIn = 27
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )
        var user = FirebaseAuth.getInstance().currentUser
    }
    init {
        FirebaseAuth.AuthStateListener {
            user = FirebaseAuth.getInstance().currentUser
        }
        if (user == null) {
            // Create and launch sign-in intent
            activity.startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers)
                    .build(),
                rcSignIn
            )
        }
    }
    fun getDisplayName(): String {
        return user?.displayName ?: ""
    }

    fun getUid(): String {
        Log.d(javaClass.simpleName, "getUid user $user uid ${user?.uid}")
        return user?.uid ?: ""
    }

    fun setDisplayNameByEmail() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val displayName = user.email?.substringBefore("@")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(javaClass.simpleName, "User profile updated.")
                }
            }
    }
}