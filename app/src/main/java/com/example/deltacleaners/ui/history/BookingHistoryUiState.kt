package com.example.deltacleaners.ui.history

import com.example.deltacleaners.data.model.Booking

data class BookingHistoryUiState(
    val isLoading: Boolean = false,
    val bookings: List<Booking> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)
