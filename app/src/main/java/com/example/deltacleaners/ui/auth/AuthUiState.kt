package com.example.deltacleaners.ui.auth

data class AuthUiState(
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val isVerificationComplete: Boolean = false,
    val isNewUser: Boolean = false,
    val error: String? = null,
    val phoneNumber: String = "",
    val name: String = "",
    val verificationId: String = ""
)