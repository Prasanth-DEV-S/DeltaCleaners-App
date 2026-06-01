package com.example.deltacleaners.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.model.Booking
import com.example.deltacleaners.data.model.Review
import com.example.deltacleaners.data.repository.BookingRepository
import com.example.deltacleaners.data.repository.ReviewRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BookingHistoryViewModel @Inject constructor(
    private val repository: BookingRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(BookingHistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        if (userId.isNotEmpty()) {
            fetchBookings()
        } else {
            _uiState.update { it.copy(error = "User not logged in") }
        }
    }

    fun fetchBookings() {
        viewModelScope.launch {
            repository.getUserBookings(userId)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { bookings ->
                    _uiState.update { it.copy(isLoading = false, bookings = bookings, error = null) }
                }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            // Keep current bookings but show loading
            _uiState.update { it.copy(isLoading = true) }
            
            val result = repository.updateBookingStatus(bookingId, "Cancelled")
            
            if (result.isSuccess) {
                // We don't need to manually update the list here because 
                // fetchBookings() is already collecting from a callbackFlow 
                // that listens to real-time Firestore snapshots.
                _uiState.update { it.copy(
                    isLoading = false,
                    successMessage = "Booking cancelled successfully"
                ) }
            } else {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Failed to cancel booking: ${result.exceptionOrNull()?.message}"
                ) }
            }
        }
    }

    fun submitReview(
        booking: Booking,
        rating: Int,
        reviewText: String,
        userName: String = "User"
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val review = Review(
                reviewId = UUID.randomUUID().toString(),
                bookingId = booking.bookingId,
                userId = userId,
                userName = userName,
                cleanerId = booking.cleanerId ?: "",
                serviceId = booking.serviceName,
                rating = rating.toFloat(),
                review = reviewText,
                createdAt = Timestamp.now()
            )
            
            val result = reviewRepository.submitReview(review)
            if (result.isSuccess) {
                _uiState.update { it.copy(
                    isLoading = false,
                    successMessage = "Review submitted successfully!"
                ) }
            } else {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Failed to submit review: ${result.exceptionOrNull()?.message}"
                ) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
