package com.example.deltacleaners.ui.booking

import app.cash.turbine.test
import com.example.deltacleaners.data.model.Booking
import com.example.deltacleaners.data.repository.BookingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: BookingRepository
    private lateinit var viewModel: BookingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createBooking success updates state to success`() = runTest {
        val booking = Booking(bookingId = "id_123")
        coEvery { repository.createBooking(any()) } returns Result.success(Unit)
        
        viewModel = BookingViewModel(repository)
        
        viewModel.uiState.test {
            // Initial state
            assertEquals(false, awaitItem().isSuccess)
            
            viewModel.createBooking(booking)
            
            // Loading state
            assertEquals(true, awaitItem().isLoading)
            
            // Success state
            val successState = awaitItem()
            assertEquals(true, successState.isSuccess)
            assertEquals(false, successState.isLoading)
        }
    }

    @Test
    fun `createBooking failure updates state with error`() = runTest {
        val booking = Booking(bookingId = "id_123")
        val errorMessage = "Network Error"
        coEvery { repository.createBooking(any()) } returns Result.failure(Exception(errorMessage))
        
        viewModel = BookingViewModel(repository)
        
        viewModel.uiState.test {
            awaitItem() // Initial
            
            viewModel.createBooking(booking)
            awaitItem() // Loading
            
            val errorState = awaitItem()
            assertEquals(errorMessage, errorState.error)
            assertEquals(false, errorState.isLoading)
        }
    }
}
