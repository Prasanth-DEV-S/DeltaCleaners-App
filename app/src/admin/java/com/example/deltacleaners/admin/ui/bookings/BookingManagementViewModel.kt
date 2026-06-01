package com.example.deltacleaners.admin.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.repository.BookingRepository
import com.example.deltacleaners.data.model.Booking
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingManagementViewModel @Inject constructor(
    private val repository: BookingRepository
) : ViewModel() {

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings = _bookings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        observeBookings()
    }

    private fun observeBookings() {
        viewModelScope.launch {
            repository.getAllBookings()
                .onStart { _isLoading.value = true }
                .catch { _isLoading.value = false }
                .collect {
                    _bookings.value = it
                    _isLoading.value = false
                }
        }
    }
}
