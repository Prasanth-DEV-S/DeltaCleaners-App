package com.example.deltacleaners.admin.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.repository.BookingRepository
import com.example.deltacleaners.data.repository.CleanerRepository
import com.example.deltacleaners.data.model.Booking
import com.example.deltacleaners.data.model.Cleaner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingDetailsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val cleanerRepository: CleanerRepository
) : ViewModel() {

    private val _booking = MutableStateFlow<Booking?>(null)
    val booking = _booking.asStateFlow()

    private val _cleaners = MutableStateFlow<List<Cleaner>>(emptyList())
    val cleaners = _cleaners.asStateFlow()

    init {
        observeCleaners()
    }

    private fun observeCleaners() {
        viewModelScope.launch {
            cleanerRepository.getCleaners().collect {
                _cleaners.value = it.filter { it.isActive }
            }
        }
    }

    fun loadBooking(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.getBookingById(bookingId).collect {
                _booking.value = it
            }
        }
    }

    fun updateStatus(status: String) {
        val id = _booking.value?.bookingId ?: return
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(id, status)
        }
    }

    fun assignCleaner(cleanerId: String) {
        val id = _booking.value?.bookingId ?: return
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(id, "Assigned", cleanerId)
        }
    }
}
