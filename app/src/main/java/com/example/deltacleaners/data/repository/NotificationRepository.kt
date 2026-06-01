package com.example.deltacleaners.data.repository

import com.example.deltacleaners.data.model.AppNotification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val notificationsCollection = firestore.collection("notifications")

    fun getUserNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val subscription = notificationsCollection
            .whereEqualTo("userId", userId)
            // .orderBy("createdAt", Query.Direction.DESCENDING) // Requires composite index
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to notifications")
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(AppNotification::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }?.sortedByDescending { it.createdAt as? Comparable<Any?> } ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error marking notification as read")
        }
    }

    fun getUnreadCount(userId: String): Flow<Int> = callbackFlow {
        val subscription = notificationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { subscription.remove() }
    }
}
