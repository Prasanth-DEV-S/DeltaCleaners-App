package com.example.deltacleaners.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PriceCalculatorTest {

    private val propertyOptions = mapOf(
        "1 BHK" to 1999.0,
        "2 BHK" to 2999.0,
        "3 BHK" to 3999.0
    )

    @Test
    fun `1 BHK house cleaning price is correct`() {
        val price = PriceCalculator.calculatePrice(
            basePrice = 999.0,
            propertyType = "1 BHK",
            propertyOptions = propertyOptions,
            bathrooms = 1
        )
        assertEquals(1999.0, price, 0.01)
    }

    @Test
    fun `2 BHK price is correct`() {
        val price = PriceCalculator.calculatePrice(
            basePrice = 999.0,
            propertyType = "2 BHK",
            propertyOptions = propertyOptions,
            bathrooms = 1
        )
        assertEquals(2999.0, price, 0.01)
    }

    @Test
    fun `bathroom quantity pricing adds 300 per extra bathroom`() {
        val price = PriceCalculator.calculatePrice(
            basePrice = 999.0,
            propertyType = "1 BHK",
            propertyOptions = propertyOptions,
            bathrooms = 3 // 2 extra bathrooms
        )
        // 1999 + (2 * 300) = 2599
        assertEquals(2599.0, price, 0.01)
    }

    @Test
    fun `invalid property type uses base price`() {
        val price = PriceCalculator.calculatePrice(
            basePrice = 999.0,
            propertyType = "Unknown",
            propertyOptions = propertyOptions,
            bathrooms = 1
        )
        assertEquals(999.0, price, 0.01)
    }

    @Test
    fun `negative bathrooms treated as zero extra charge`() {
        val price = PriceCalculator.calculatePrice(
            basePrice = 999.0,
            propertyType = "1 BHK",
            propertyOptions = propertyOptions,
            bathrooms = -5
        )
        assertEquals(1999.0, price, 0.01)
    }
}
