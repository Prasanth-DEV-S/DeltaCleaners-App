package com.example.deltacleaners.data.repository

import com.example.deltacleaners.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun getUser(uid: String): Result<User?> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            Result.success(snapshot.toObject(User::class.java))
        } catch (e: Exception) {
            Timber.e(e, "Error getting user")
            Result.failure(e)
        }
    }

    suspend fun createUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAddress(uid: String, address: String): Result<Unit> {
        return try {
            val user = getUser(uid).getOrNull()
            val updatedAddresses = (user?.savedAddresses ?: emptyList()) + address
            usersCollection.document(uid).update("savedAddresses", updatedAddresses).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
