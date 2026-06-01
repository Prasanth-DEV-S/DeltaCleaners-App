package com.example.deltacleaners.data.repository

import com.example.deltacleaners.data.model.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val bookingsCollection = firestore.collection("bookings")

    suspend fun createBooking(booking: Booking): Result<Unit> {
        return try {
            bookingsCollection
                .document(booking.bookingId)
                .set(booking)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserBookings(userId: String): Flow<List<Booking>> = callbackFlow {
        val listener = bookingsCollection
            .whereEqualTo("userId", userId)
            // .orderBy("createdAt", Query.Direction.DESCENDING) // Requires composite index
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Booking::class.java)
                    } catch (e: Exception) {
                        Timber.e(e, "Error deserializing booking ${doc.id}")
                        null
                    }
                }?.sortedByDescending { it.createdAt as? Comparable<Any?> } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    fun getAvailableBookings(): Flow<List<Booking>> = callbackFlow {
        val listener = bookingsCollection
            .whereEqualTo("status", "Pending")
            // .orderBy("createdAt", Query.Direction.DESCENDING) // Requires composite index
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Booking::class.java)
                    } catch (e: Exception) {
                        Timber.e(e, "Error deserializing booking ${doc.id}")
                        null
                    }
                }?.sortedByDescending { it.createdAt as? Comparable<Any?> } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    fun getCleanerBookings(cleanerId: String): Flow<List<Booking>> = callbackFlow {
        val listener = bookingsCollection
            .whereEqualTo("cleanerId", cleanerId)
            // .orderBy("createdAt", Query.Direction.DESCENDING) // Requires composite index
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Booking::class.java)
                    } catch (e: Exception) {
                        Timber.e(e, "Error deserializing booking ${doc.id}")
                        null
                    }
                }?.sortedByDescending { it.createdAt as? Comparable<Any?> } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String, cleanerId: String? = null): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to status)
            cleanerId?.let { updates["cleanerId"] = it }
            bookingsCollection.document(bookingId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun generateBookingId(): String {
        return UUID.randomUUID().toString()
    }

    fun getAllBookings(): Flow<List<Booking>> = callbackFlow {
        val listener = bookingsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Booking::class.java)
                    } catch (e: Exception) {
                        Timber.e(e, "Error deserializing booking ${doc.id}")
                        null
                    }
                } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    fun getBookingById(bookingId: String): Flow<Booking?> = callbackFlow {
        val listener = bookingsCollection.document(bookingId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            try {
                trySend(snapshot?.toObject(Booking::class.java))
            } catch (e: Exception) {
                Timber.e(e, "Error deserializing booking $bookingId")
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }
}
