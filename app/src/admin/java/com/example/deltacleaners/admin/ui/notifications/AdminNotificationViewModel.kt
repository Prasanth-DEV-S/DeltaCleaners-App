package com.example.deltacleaners.admin.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.model.AppNotification
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AdminNotificationViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    fun sendNotification(title: String, message: String) {
        if (title.isBlank() || message.isBlank()) return
        
        viewModelScope.launch {
            val notification = AppNotification(
                notificationId = UUID.randomUUID().toString(),
                userId = "broadcast", // Marker for all users
                title = title,
                message = message,
                type = "admin_announcement",
                isRead = false,
                createdAt = Timestamp.now()
            )
            
            firestore.collection("notifications").document(notification.notificationId)
                .set(notification)
        }
    }
}
