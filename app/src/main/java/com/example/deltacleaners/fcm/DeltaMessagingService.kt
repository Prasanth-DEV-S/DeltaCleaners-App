package com.example.deltacleaners.fcm

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DeltaMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM Token: $token")
        updateTokenOnServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("From: ${message.from}")

        val title = message.notification?.title ?: message.data["title"] ?: "Delta Cleaners"
        val body = message.notification?.body ?: message.data["message"] ?: ""
        
        NotificationHelper(this).showNotification(title, body, message.data)
    }

    private fun updateTokenOnServer(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .update("fcmToken", token)
            .addOnFailureListener { e ->
                Timber.e(e, "Error updating FCM token")
            }
    }
}
