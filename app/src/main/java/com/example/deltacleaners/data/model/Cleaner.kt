package com.example.deltacleaners.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Cleaner(
    val cleanerId: String = "",
    val name: String = "",
    val phone: String = "",
    val profileImage: String = "",
    val rating: Double = 0.0,
    val city: String = "",
    val services: List<String> = emptyList(),
    val availability: Boolean = true,
    val isActive: Boolean = true,
    val totalJobs: Int = 0,
    val earnings: Double = 0.0,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)
