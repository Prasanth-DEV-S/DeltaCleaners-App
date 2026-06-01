package com.example.deltacleaners.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImage: String = "",
    val role: String = "customer",
    val isBlocked: Boolean = false,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val totalBookings: Long = 0,
    val walletBalance: Double = 0.0,
    val savedAddresses: List<String> = emptyList()
) {
    @get:Exclude
    val createdAtTimestamp: Timestamp?
        get() = createdAt
}
