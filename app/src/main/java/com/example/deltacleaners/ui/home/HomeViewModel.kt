package com.example.deltacleaners.ui.home

import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.location.LocationHelper
import com.example.deltacleaners.data.model.Service
import com.example.deltacleaners.data.repository.BannerRepository
import com.example.deltacleaners.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationHelper: LocationHelper,
    private val serviceRepository: ServiceRepository,
    private val bannerRepository: BannerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _allServices = MutableStateFlow<List<Service>>(emptyList())

    val filteredServices = _uiState
        .map { it.searchQuery }
        .debounce(300)
        .distinctUntilChanged()
        .combine(_allServices) { query, allServices ->
            if (query.isBlank()) {
                allServices
            } else {
                allServices.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchServices()
        fetchBanners()
    }

    private fun fetchServices() {
        viewModelScope.launch {
            serviceRepository.getActiveServices()
                .onStart { _uiState.update { it.copy(isServicesLoading = true) } }
                .catch { e ->
                    Timber.e(e, "Error fetching services")
                    _uiState.update { it.copy(isServicesLoading = false, error = "Error fetching services: ${e.message}") }
                }
                .collect { list ->
                    _allServices.value = list
                    _uiState.update { it.copy(isServicesLoading = false, services = list) }
                }
        }
    }

    private fun fetchBanners() {
        viewModelScope.launch {
            bannerRepository.getActiveBanners()
                .onStart { _uiState.update { it.copy(isBannersLoading = true) } }
                .catch { e ->
                    Timber.e(e, "Error fetching banners")
                    _uiState.update { it.copy(isBannersLoading = false) }
                }
                .collect { list ->
                    _uiState.update { it.copy(isBannersLoading = false, banners = list) }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun fetchLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocationLoading = true, error = null) }
            val location = locationHelper.getCurrentLocation()
            if (location != null) {
                _uiState.update { it.copy(
                    isLocationLoading = false,
                    locationData = location,
                    selectedAddress = "${location.street}, ${location.city}"
                ) }
            } else {
                _uiState.update { it.copy(
                    isLocationLoading = false,
                    error = "Unable to fetch location. Please enter manually."
                ) }
            }
        }
    }

    fun updateSelectedAddress(address: String) {
        _uiState.update { it.copy(selectedAddress = address) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
