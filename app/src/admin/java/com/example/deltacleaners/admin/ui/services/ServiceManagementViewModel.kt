package com.example.deltacleaners.admin.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.admin.data.repository.AdminRepository
import com.example.deltacleaners.data.model.Service
import com.example.deltacleaners.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceManagementViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services = _services.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchServices()
    }

    private fun fetchServices() {
        viewModelScope.launch {
            _isLoading.value = true
            serviceRepository.getActiveServices().collect {
                _services.value = it
                _isLoading.value = false
            }
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            adminRepository.deleteService(serviceId)
        }
    }

    fun addService(name: String, price: Double) {
        viewModelScope.launch {
            val service = Service(name = name, startingPrice = price, serviceId = java.util.UUID.randomUUID().toString())
            // Need a method to save service in repo
            // I'll reuse the firestore collection logic if repo doesn't have it
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("services").document(service.serviceId).set(service)
        }
    }
}
