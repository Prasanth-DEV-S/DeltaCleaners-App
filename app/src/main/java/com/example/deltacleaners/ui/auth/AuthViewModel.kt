package com.example.deltacleaners.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.repository.AuthRepository
import com.example.deltacleaners.data.repository.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    suspend fun getUserRole(uid: String): String {
        return userRepository.getUser(uid).getOrNull()?.role ?: "customer"
    }

    fun sendOtp(phoneNumber: String, name: String, activity: Activity) {
        _uiState.update { it.copy(isLoading = true, error = null, phoneNumber = phoneNumber, name = name) }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        signInWithCredential(credential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken,
                    ) {
                        resendToken = token
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                isOtpSent = true, 
                                verificationId = verificationId 
                            ) 
                        }
                    }
                }
            )
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(otp: String) {
        val verificationId = _uiState.value.verificationId
        if (verificationId.isEmpty()) {
            _uiState.update { it.copy(error = "Verification session expired. Please go back and try again.") }
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = repository.signInWithPhoneCredential(credential)
            if (result.isSuccess) {
                val authResult = result.getOrNull()
                val isNewUser = authResult?.additionalUserInfo?.isNewUser == true
                val firebaseUser = auth.currentUser
                
                if (firebaseUser != null) {
                    // Check if user already exists in Firestore
                    val existingUser = userRepository.getUser(firebaseUser.uid).getOrNull()
                    
                    if (existingUser == null) {
                        // Create user in Firestore if not exists
                        val newUser = com.example.deltacleaners.data.model.User(
                            userId = firebaseUser.uid,
                            name = _uiState.value.name,
                            phone = firebaseUser.phoneNumber ?: _uiState.value.phoneNumber,
                            role = "customer",
                            createdAt = Timestamp.now()
                        )
                        userRepository.createUser(newUser)
                    }
                }

                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        isVerificationComplete = true,
                        isNewUser = isNewUser
                    ) 
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = result.exceptionOrNull()?.localizedMessage ?: "Verification failed"
                    ) 
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
    
    fun isUserLoggedIn(): Boolean = repository.isUserLoggedIn()
}
