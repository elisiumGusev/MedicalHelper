package com.luckylittlesparrow.healthassistant.authlib.data

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.luckylittlesparrow.core_util.concurrency.AppDispatchers
import com.luckylittlesparrow.healthassistant.authlib.data.mapper.FirebaseUserToAppUserMapper
import com.luckylittlesparrow.healthassistant.authlib.data.service.AuthService
import com.luckylittlesparrow.healthassistant.authlib.domain.AuthRepository
import com.luckylittlesparrow.healthassistant.feature_account_api.model.User
import com.orhanobut.logger.Logger
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * @author Gusev Andrei
 * @since  1.0
 */
class AuthRepositoryImpl(
    private val authService: AuthService,
    private val dispatchers: AppDispatchers,
    private val mapper: FirebaseUserToAppUserMapper
) : AuthRepository {

    override suspend fun signIn(username: String, password: String) = withContext(dispatchers.IO) {
        val result = authService.signIn(username, password).await()
        val user = result.user
        return@withContext if (user == null) null else mapper.map(user)
    }

    override suspend fun signUp(username: String, email: String, password: String) = withContext(dispatchers.IO) {
        var user = authService.currentUser()

        val result: AuthResult = if (user != null) {
            user.linkWithCredential(EmailAuthProvider.getCredential(email, password)).await()
        } else authService.signUp(email, password).await()
//
//        authService.signUp(email, password).addOnCompleteListener {
//            Logger.d(it)
//            if (it.isSuccessful) user = it.result!!.user
//        }
//

        authService.currentUser()?.updateProfile(
            UserProfileChangeRequest
                .Builder()
                .setDisplayName(username)
                .build()
        )

        user = result.user

        return@withContext if (user == null) null else mapper.map(user)
    }

    override suspend fun signInAnonymously() = withContext(dispatchers.IO) {
        val result = authService.signInAnonymously().await()
        Logger.d(result)
        return@withContext (result != null)
    }

    override suspend fun isAuthenticated() = withContext(dispatchers.IO) {
        val user = authService.currentUser()
        Logger.d(user)
        return@withContext user != null
    }

    override suspend fun signOut() = withContext(dispatchers.IO) {
        authService.signOut()
        return@withContext true
    }

    override suspend fun signInWithGoogle(data: Intent?) = withContext(dispatchers.IO) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.getResult(ApiException::class.java)
        return@withContext firebaseAuthWithGoogle(account)
    }

    private suspend fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?): User? {
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        val result = authService.signInWithCredential(credential).await()
        val user = result.user
        return if (user == null) null else mapper.map(user)
    }
}