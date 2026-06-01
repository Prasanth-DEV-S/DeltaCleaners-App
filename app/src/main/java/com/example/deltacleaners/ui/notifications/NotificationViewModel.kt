package com.example.deltacleaners.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.model.AppNotification
import com.example.deltacleaners.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val unreadCount = notificationRepository.getUnreadCount(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        if (userId.isNotEmpty()) {
            observeNotifications()
        }
    }

    private fun observeNotifications() {
        notificationRepository.getUserNotifications(userId)
            .onEach { 
                _notifications.value = it 
                _error.value = null
            }
            .catch { e -> 
                Timber.e(e, "Error observing notifications")
                _error.value = e.message
            }
            .launchIn(viewModelScope)
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }
}
