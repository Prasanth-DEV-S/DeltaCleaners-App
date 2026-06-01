package com.example.deltacleaners.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class Review(
    val reviewId: String = "",
    val bookingId: String = "",
    val userId: String = "",
    val userName: String = "",
    val cleanerId: String = "",
    val serviceId: String = "",
    val rating: Float = 0f,
    val review: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    @get:Exclude
    val createdAtTimestamp: Timestamp?
        get() = createdAt
}
