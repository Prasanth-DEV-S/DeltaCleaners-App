package com.example.deltacleaners.admin.ui.cleaners

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.repository.CleanerRepository
import com.example.deltacleaners.data.model.Cleaner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CleanerManagementViewModel @Inject constructor(
    private val repository: CleanerRepository
) : ViewModel() {

    private val _cleaners = MutableStateFlow<List<Cleaner>>(emptyList())
    val cleaners = _cleaners.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        observeCleaners()
    }

    private fun observeCleaners() {
        viewModelScope.launch {
            repository.getCleaners()
                .onStart { _isLoading.value = true }
                .catch { _isLoading.value = false }
                .collect {
                    _cleaners.value = it
                    _isLoading.value = false
                }
        }
    }

    fun addCleaner(name: String, phone: String, city: String) {
        viewModelScope.launch {
            val cleaner = Cleaner(name = name, phone = phone, city = city)
            repository.addCleaner(cleaner)
        }
    }

    fun deleteCleaner(cleanerId: String) {
        viewModelScope.launch {
            repository.deleteCleaner(cleanerId)
        }
    }

    fun toggleCleanerStatus(cleanerId: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleCleanerStatus(cleanerId, isActive)
        }
    }

    fun updateCleaner(cleaner: Cleaner) {
        viewModelScope.launch {
            repository.updateCleaner(cleaner)
        }
    }
}
