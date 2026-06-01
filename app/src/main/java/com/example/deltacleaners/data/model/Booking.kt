package com.example.deltacleaners.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class Booking(
    val bookingId: String = "",
    val userId: String = "",
    val serviceName: String = "",
    val address: String = "",
    val date: String = "",
    val time: String = "",
    val notes: String = "",
    val status: String = "Pending",
    val cleanerId: String? = null,
    val price: String = "0.00",
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    @get:Exclude
    val createdAtTimestamp: Timestamp?
        get() = createdAt
}
