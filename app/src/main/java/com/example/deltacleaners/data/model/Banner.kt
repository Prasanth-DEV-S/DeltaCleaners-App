package com.example.deltacleaners.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class Banner(
    val bannerId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val enabled: Boolean = true,
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    @get:Exclude
    val createdAtTimestamp: Timestamp?
        get() = createdAt
}
