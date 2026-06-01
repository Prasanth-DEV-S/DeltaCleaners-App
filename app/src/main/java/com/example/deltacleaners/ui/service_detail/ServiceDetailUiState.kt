package com.example.deltacleaners.ui.service_detail

import com.example.deltacleaners.data.model.FaqItem
import com.example.deltacleaners.data.model.Review

data class ServiceDetailUiState(
    val serviceName: String = "",
    val bannerImage: String = "",
    val icon: String = "",
    val basePrice: Double = 0.0,
    val propertyType: String = "",
    val bathrooms: Int = 1,
    val squareFeet: Int = 500,
    val calculatedPrice: Double = 0.0,
    val estimatedDuration: String = "",
    val included: List<String> = emptyList(),
    val notIncluded: List<String> = emptyList(),
    val propertyOptions: Map<String, Double> = emptyMap(),
    val faq: List<FaqItem> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val inclusionsExpanded: Boolean = true,
    val exclusionsExpanded: Boolean = false,
    val expandedFaqIndex: Int? = null,
    val isPriceCalculated: Boolean = false
)
