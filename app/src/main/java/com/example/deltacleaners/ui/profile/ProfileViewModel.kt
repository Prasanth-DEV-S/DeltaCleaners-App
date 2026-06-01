package com.example.deltacleaners.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.model.Review
import com.example.deltacleaners.data.model.User
import com.example.deltacleaners.data.repository.ReviewRepository
import com.example.deltacleaners.data.repository.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener {
        fetchUser()
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }

    fun fetchUser() {
        val currentUid = auth.currentUser?.uid ?: ""
        if (currentUid.isEmpty()) {
            _user.value = null
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = userRepository.getUser(currentUid)
            _user.value = result.getOrNull()
            _isLoading.value = false
        }
    }

    fun addAddress(fullAddress: String) {
        viewModelScope.launch {
            userRepository.addAddress(userId, fullAddress)
            fetchUser()
        }
    }

    fun submitGeneralReview(rating: Int, comment: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val review = Review(
                reviewId = UUID.randomUUID().toString(),
                userId = userId,
                userName = _user.value?.name ?: "User",
                rating = rating.toFloat(),
                review = comment,
                createdAt = Timestamp.now()
            )
            val result = reviewRepository.submitReview(review)
            if (result.isSuccess) {
                _message.value = "Thank you for your review!"
            } else {
                _message.value = "Failed to submit review"
            }
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun logout() {
        auth.signOut()
    }
}
