package com.example.deltacleaners.ui.cleaner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.model.Booking
import com.example.deltacleaners.data.repository.BookingRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CleanerViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    val cleanerId = auth.currentUser?.uid ?: ""

    private val _availableBookings = MutableStateFlow<List<Booking>>(emptyList())
    val availableBookings = _availableBookings.asStateFlow()

    private val _myBookings = MutableStateFlow<List<Booking>>(emptyList())
    val myBookings = _myBookings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchBookings()
    }

    private fun fetchBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Available (Pending)
            bookingRepository.getAvailableBookings()
                .collect { bookings ->
                    _availableBookings.value = bookings
                }
        }
        
        viewModelScope.launch {
            // My active jobs
            bookingRepository.getCleanerBookings(cleanerId)
                .collect { bookings ->
                    _myBookings.value = bookings
                    _isLoading.value = false
                }
        }
    }

    fun acceptBooking(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(bookingId, "Confirmed", cleanerId)
        }
    }

    fun updateStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            bookingRepository.updateBookingStatus(bookingId, status)
        }
    }
}
