package com.example.deltacleaners.admin.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.model.User
import com.example.deltacleaners.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AdminRegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminRegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val ADMIN_SECRET_CODE = "DELTA_ADMIN_2026"

    private val _uiState = MutableStateFlow(AdminRegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun registerAdmin(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        secretCode: String
    ) {
        // Validation
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() || secretCode.isBlank()) {
            _uiState.update { it.copy(error = "Please fill all fields") }
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(error = "Please enter a valid email address") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        if (password != confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }

        if (secretCode != ADMIN_SECRET_CODE) {
            _uiState.update { it.copy(error = "Invalid Admin Access Code") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // 1. Create User in Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid ?: throw Exception("Failed to get user ID")

                // 2. Create User document in Firestore
                val adminUser = User(
                    userId = uid,
                    name = name,
                    email = email,
                    role = "admin"
                )

                userRepository.createUser(adminUser).fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    },
                    onFailure = { e ->
                        // If Firestore fails, we might want to delete the Auth user, 
                        // but for simplicity here we just show error
                        _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
