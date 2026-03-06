package com.brwnkid.homeinventory.data.sync

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.tasks.await

class GoogleDriveAuthManager(private val context: Context) {
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
        .build()

    private val signInClient = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent {
        return signInClient.signInIntent
    }

    @Throws(ApiException::class)
    fun getSignedInAccountFromIntent(intent: Intent?): GoogleSignInAccount {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        return task.getResult(ApiException::class.java)
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    suspend fun signOut() {
        try {
            signInClient.signOut().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCredential(): GoogleAccountCredential? {
        val account = getSignedInAccount()?.account ?: return null
        return GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        ).apply {
            selectedAccount = account
        }
    }
}
