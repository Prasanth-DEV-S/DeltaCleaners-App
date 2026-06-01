package com.example.deltacleaners.ui.home

import com.example.deltacleaners.data.location.LocationData
import com.example.deltacleaners.data.model.Banner
import com.example.deltacleaners.data.model.Service

data class HomeUiState(
    val selectedAddress: String = "Select Location",
    val locationData: LocationData? = null,
    val isLocationLoading: Boolean = false,
    val services: List<Service> = emptyList(),
    val isServicesLoading: Boolean = false,
    val banners: List<Banner> = emptyList(),
    val isBannersLoading: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null
)
