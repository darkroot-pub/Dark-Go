package com.example.darkgo.data.repository

import com.example.darkgo.data.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://darkgo-72baf-default-rtdb.firebaseio.com")
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUid: String?
        get() = auth.currentUser?.uid

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun registerWithEmail(email: String, pass: String, displayName: String): Result<UserProfile> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("User creation failed")
            val profile = UserProfile(
                uid = user.uid,
                displayName = displayName.ifBlank { email.substringBefore("@") },
                email = email.trim(),
                photoUrl = null,
                createdAt = System.currentTimeMillis()
            )
            database.reference.child("users").child(user.uid).setValue(profile).await()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("Login failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveOrUpdateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            database.reference.child("users").child(profile.uid).setValue(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(displayName: String, bio: String, avatarId: String): Result<UserProfile> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not authenticated")
            val cleanName = displayName.trim()
            if (cleanName.isNotBlank()) {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(cleanName)
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            val snapshot = database.reference.child("users").child(user.uid).get().await()
            val existing = snapshot.getValue(UserProfile::class.java)
                ?: UserProfile(
                    uid = user.uid,
                    email = user.email ?: "",
                    createdAt = System.currentTimeMillis()
                )

            val updated = existing.copy(
                displayName = cleanName.ifBlank { existing.displayName },
                bio = bio.trim(),
                avatarId = avatarId
            )
            database.reference.child("users").child(user.uid).setValue(updated).await()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserProfile(uid: String): Result<UserProfile> {
        return try {
            val snapshot = database.reference.child("users").child(uid).get().await()
            val profile = snapshot.getValue(UserProfile::class.java)
                ?: UserProfile(
                    uid = uid,
                    displayName = auth.currentUser?.displayName ?: "Player",
                    email = auth.currentUser?.email ?: "",
                    createdAt = System.currentTimeMillis()
                )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
