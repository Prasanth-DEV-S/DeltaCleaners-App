package com.example.deltacleaners.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class Service(
    val serviceId: String = "",
    val name: String = "",
    val description: String = "",
    val bannerImage: String = "",
    val icon: String = "CleaningServices",
    val pricingType: String = "dynamic",
    val startingPrice: Double = 0.0,
    val duration: String = "",
    val included: List<String> = emptyList(),
    val notIncluded: List<String> = emptyList(),
    val propertyOptions: Map<String, Double> = emptyMap(),
    val faq: List<FaqItem> = emptyList(),
    val enabled: Boolean = true,
    val rating: Double = 5.0,
    val totalReviews: Int = 0,
    val category: String = "General",
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    @get:Exclude
    val createdAtTimestamp: Timestamp?
        get() = createdAt
}

data class FaqItem(
    val question: String = "",
    val answer: String = ""
)
