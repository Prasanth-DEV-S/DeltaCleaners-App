package com.example.deltacleaners.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.location.LocationHelper
import com.example.deltacleaners.data.model.Booking
import com.example.deltacleaners.data.repository.BookingRepository
import com.example.deltacleaners.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val repository: BookingRepository,
    private val userRepository: UserRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    private val _uiState =
        MutableStateFlow(BookingUiState())

    val uiState =
        _uiState.asStateFlow()

    private val _suggestedAddress = MutableStateFlow("")
    val suggestedAddress = _suggestedAddress.asStateFlow()

    init {
        loadDefaultAddress()
    }

    fun loadDefaultAddress() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            // 1. Try saved address from profile
            val user = userRepository.getUser(userId).getOrNull()
            if (user != null && user.savedAddresses.isNotEmpty()) {
                _suggestedAddress.value = user.savedAddresses.first()
            } else {
                // 2. Fallback to current location
                val location = locationHelper.getCurrentLocation()
                if (location != null) {
                    _suggestedAddress.value = "${location.street}, ${location.city}"
                }
            }
        }
    }

    fun createBooking(
        booking: Booking
    ) {

        viewModelScope.launch {

            _uiState.value =
                BookingUiState(
                    isLoading = true
                )

            val result =
                repository.createBooking(
                    booking
                )

            _uiState.value =
                if (result.isSuccess) {

                    BookingUiState(
                        isSuccess = true
                    )

                } else {

                    BookingUiState(
                        error =
                            result.exceptionOrNull()?.message
                    )
                }
        }
    }

    fun generateBookingId(): String {
        return repository.generateBookingId()
    }
}