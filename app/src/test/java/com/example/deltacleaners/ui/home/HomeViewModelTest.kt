package com.example.deltacleaners.ui.home

import app.cash.turbine.test
import com.example.deltacleaners.data.location.LocationData
import com.example.deltacleaners.data.location.LocationHelper
import com.example.deltacleaners.data.model.Service
import com.example.deltacleaners.data.repository.ServiceRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var locationHelper: LocationHelper
    private lateinit var serviceRepository: ServiceRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        locationHelper = mockk()
        serviceRepository = mockk()
        
        every { serviceRepository.getActiveServices() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        viewModel = HomeViewModel(locationHelper, serviceRepository)
        
        viewModel.isServicesLoading.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `fetchLocation success updates selectedAddress`() = runTest {
        val mockLocation = LocationData("MG Road", "Bengaluru", 12.97, 77.59)
        coEvery { locationHelper.getCurrentLocation() } returns mockLocation
        
        viewModel = HomeViewModel(locationHelper, serviceRepository)
        viewModel.fetchLocation()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("MG Road, Bengaluru", viewModel.selectedAddress.value)
    }

    @Test
    fun `fetchLocation failure updates errorState`() = runTest {
        coEvery { locationHelper.getCurrentLocation() } returns null
        
        viewModel = HomeViewModel(locationHelper, serviceRepository)
        viewModel.fetchLocation()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("Unable to fetch location. Please enter manually.", viewModel.errorState.value)
    }

    @Test
    fun `search query filters services correctly`() = runTest {
        val services = listOf(
            Service(name = "House Cleaning"),
            Service(name = "Sofa Cleaning"),
            Service(name = "Bathroom Cleaning")
        )
        every { serviceRepository.getActiveServices() } returns flowOf(services)
        
        viewModel = HomeViewModel(locationHelper, serviceRepository)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.onSearchQueryChange("Sofa")
        
        viewModel.services.test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Sofa Cleaning", result[0].name)
        }
    }
}
