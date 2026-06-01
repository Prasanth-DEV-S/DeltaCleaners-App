package com.example.deltacleaners.admin.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AdminAuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminAuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(AdminAuthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            validateAdminRole(currentUser.uid)
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter email and password") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                validateAdminRole(result.user?.uid ?: "")
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private fun validateAdminRole(uid: String) {
        viewModelScope.launch {
            val userResult = userRepository.getUser(uid)
            userResult.fold(
                onSuccess = { user ->
                    if (user?.role == "admin") {
                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    } else {
                        auth.signOut()
                        _uiState.update { it.copy(isLoading = false, error = "Access Denied: Not an admin account") }
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
            )
        }
    }

    fun logout() {
        auth.signOut()
        _uiState.update { it.copy(isLoggedIn = false) }
    }
}
