package com.example.deltacleaners.data.repository

import com.example.deltacleaners.data.model.Cleaner
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CleanerRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val cleanerCollection = firestore.collection("cleaners")

    fun getCleaners(): Flow<List<Cleaner>> = callbackFlow {
        val subscription = cleanerCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val cleaners = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Cleaner::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(cleaners)
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun addCleaner(cleaner: Cleaner): Result<Unit> {
        return try {
            val docRef = cleanerCollection.document()
            val cleanerWithId = cleaner.copy(cleanerId = docRef.id)
            docRef.set(cleanerWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCleaner(cleaner: Cleaner): Result<Unit> {
        return try {
            cleanerCollection.document(cleaner.cleanerId).set(cleaner).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCleaner(cleanerId: String): Result<Unit> {
        return try {
            cleanerCollection.document(cleanerId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleCleanerStatus(cleanerId: String, isActive: Boolean): Result<Unit> {
        return try {
            cleanerCollection.document(cleanerId).update("isActive", isActive).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
