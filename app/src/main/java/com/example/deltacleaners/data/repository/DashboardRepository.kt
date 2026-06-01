package com.example.deltacleaners.data.repository

import com.example.deltacleaners.data.model.Booking
import com.example.deltacleaners.data.model.Cleaner
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getDashboardData(): Flow<DashboardData> = combine(
        getAllBookings(),
        getCollectionCount("users"),
        getCollectionCount("cleaners", "isActive", true),
        getCollectionCount("reviews")
    ) { bookings, usersCount, activeCleanersCount, reviewsCount ->
        val totalRevenue = bookings.filter { it.status == "Completed" }
            .sumOf { it.price.toDoubleOrNull() ?: 0.0 }
        
        DashboardData(
            totalBookings = bookings.size,
            pendingBookings = bookings.count { it.status == "Pending" },
            completedBookings = bookings.count { it.status == "Completed" },
            cancelledBookings = bookings.count { it.status == "Cancelled" },
            totalRevenue = totalRevenue,
            totalUsers = usersCount,
            activeCleaners = activeCleanersCount,
            totalReviews = reviewsCount,
            recentBookings = bookings.take(10)
        )
    }

    private fun getAllBookings(): Flow<List<Booking>> = callbackFlow {
        val listener = firestore.collection("bookings")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Booking::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    private fun getCollectionCount(collectionPath: String, field: String? = null, value: Any? = null): Flow<Int> = callbackFlow {
        val query = if (field != null && value != null) {
            firestore.collection(collectionPath).whereEqualTo(field, value)
        } else {
            firestore.collection(collectionPath)
        }
        val listener = query.addSnapshotListener { snapshot, _ ->
            trySend(snapshot?.size() ?: 0)
        }
        awaitClose { listener.remove() }
    }
}

data class DashboardData(
    val totalBookings: Int = 0,
    val pendingBookings: Int = 0,
    val completedBookings: Int = 0,
    val cancelledBookings: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalUsers: Int = 0,
    val activeCleaners: Int = 0,
    val totalReviews: Int = 0,
    val recentBookings: List<Booking> = emptyList()
)
