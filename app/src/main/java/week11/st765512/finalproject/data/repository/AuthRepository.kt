/**
 * File: AuthRepository.kt
 * 
 * Repository for Firebase Authentication operations. Handles user sign in, registration,
 * password reset, Google sign in, and sign out functionality.
 */
package week11.st765512.finalproject.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import week11.st765512.finalproject.data.model.Result

class AuthRepository(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val isUserSignedIn: Boolean
        get() = currentUser != null

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            Result.Loading
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.Success(it)
            } ?: Result.Error(Exception("Sign in failed: User is null"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            Result.Loading
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.Success(it)
            } ?: Result.Error(Exception("Registration failed: User is null"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            Result.Loading
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            Result.Loading
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user?.let {
                Result.Success(it)
            } ?: Result.Error(Exception("Google sign in failed: User is null"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}

