package com.example.deltacleaners.ui.service_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deltacleaners.data.repository.ReviewRepository
import com.example.deltacleaners.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun initService(name: String) {
        viewModelScope.launch {
            try {
                // Fetch service details once (or live if you prefer)
                val services = serviceRepository.getActiveServices().first()
                val remoteService = services.find { it.name == name }
                
                remoteService?.let { service ->
                    val optionsMap = service.propertyOptions
                    val firstOption = service.propertyOptions.keys.firstOrNull() ?: ""
                    
                    _uiState.update { it.copy(
                        serviceName = service.name,
                        bannerImage = service.bannerImage,
                        icon = service.icon,
                        basePrice = service.startingPrice,
                        propertyType = firstOption,
                        included = service.included,
                        notIncluded = service.notIncluded,
                        propertyOptions = optionsMap,
                        faq = service.faq,
                        estimatedDuration = service.duration
                    ) }
                    calculatePrice()
                    
                    // Start collecting dynamic reviews
                    if (service.serviceId.isNotEmpty()) {
                        observeReviews(service.serviceId)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error initializing service: $name")
            }
        }
    }

    private fun observeReviews(serviceId: String) {
        reviewRepository.getServiceReviewsFlow(serviceId)
            .onEach { reviewList ->
                val count = reviewList.size
                val average = if (count > 0) {
                    reviewList.map { it.rating }.average()
                } else 0.0
                
                _uiState.update { it.copy(
                    reviews = reviewList,
                    averageRating = average,
                    reviewCount = count
                ) }
            }
            .catch { e ->
                Timber.e(e, "Error observing reviews for service: $serviceId")
            }
            .launchIn(viewModelScope)
    }

    fun updatePropertyType(type: String) {
        _uiState.update { it.copy(propertyType = type) }
        calculatePrice()
    }

    fun updateBathrooms(count: Int) {
        _uiState.update { it.copy(bathrooms = count) }
        calculatePrice()
    }

    fun toggleInclusions() {
        _uiState.update { it.copy(inclusionsExpanded = !it.inclusionsExpanded) }
    }

    fun toggleExclusions() {
        _uiState.update { it.copy(exclusionsExpanded = !it.exclusionsExpanded) }
    }

    fun toggleFaq(index: Int) {
        _uiState.update { 
            it.copy(expandedFaqIndex = if (it.expandedFaqIndex == index) null else index)
        }
    }

    private fun calculatePrice() {
        _uiState.update { state ->
            val total = com.example.deltacleaners.domain.util.PriceCalculator.calculatePrice(
                basePrice = state.basePrice,
                propertyType = state.propertyType,
                propertyOptions = state.propertyOptions,
                bathrooms = state.bathrooms
            )
            
            state.copy(
                calculatedPrice = total,
                isPriceCalculated = true
            )
        }
    }
}
