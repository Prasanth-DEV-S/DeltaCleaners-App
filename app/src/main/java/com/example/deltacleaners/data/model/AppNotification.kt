package com.example.deltacleaners.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class AppNotification(
    val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // booking_confirmed, cleaner_assigned, etc.
    val isRead: Boolean = false,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val data: Map<String, String> = emptyMap() // For extra navigation data like bookingId
) {
    @get:Exclude
    val createdAtTimestamp: Timestamp?
        get() = createdAt
}
