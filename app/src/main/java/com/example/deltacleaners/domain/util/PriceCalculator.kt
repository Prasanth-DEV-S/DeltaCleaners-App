package com.example.deltacleaners.domain.util

object PriceCalculator {
    private const val ADDITIONAL_BATHROOM_CHARGE = 300.0

    fun calculatePrice(
        basePrice: Double,
        propertyType: String,
        propertyOptions: Map<String, Double>,
        bathrooms: Int
    ): Double {
        val selectedOptionPrice = propertyOptions[propertyType] ?: basePrice
        val bathroomAddon = (bathrooms - 1).coerceAtLeast(0) * ADDITIONAL_BATHROOM_CHARGE
        return selectedOptionPrice + bathroomAddon
    }
}
