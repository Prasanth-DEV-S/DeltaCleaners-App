package com.example.deltacleaners.admin.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.repository.DashboardData
import com.example.deltacleaners.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _dashboardData = MutableStateFlow(DashboardData())
    val dashboardData = _dashboardData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        observeDashboardData()
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            repository.getDashboardData()
                .onStart { _isLoading.value = true }
                .catch { _isLoading.value = false }
                .collect {
                    _dashboardData.value = it
                    _isLoading.value = false
                }
        }
    }

    fun refresh() {
        observeDashboardData()
    }
}
