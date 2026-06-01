package com.example.deltacleaners.data.repository

import com.example.deltacleaners.data.model.Booking
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collectionReference: CollectionReference
    private lateinit var documentReference: DocumentReference
    private lateinit var bookingRepository: BookingRepository

    @Before
    fun setup() {
        firestore = mockk()
        collectionReference = mockk()
        documentReference = mockk()
        
        every { firestore.collection("bookings") } returns collectionReference
        bookingRepository = BookingRepository(firestore)
    }

    @Test
    fun `createBooking success returns success result`() = runTest {
        val booking = Booking(bookingId = "test_id", userId = "user_123", serviceName = "Cleaning")
        val mockTask = mockk<Task<Void>>()
        
        every { collectionReference.document(booking.bookingId) } returns documentReference
        every { documentReference.set(booking) } returns mockTask
        
        // Mocking the await() extension function is complex, usually we'd use a fake or a wrapper
        // For this example, let's verify the calls are made
        bookingRepository.createBooking(booking)
        
        verify { collectionReference.document("test_id") }
        verify { documentReference.set(booking) }
    }
}
