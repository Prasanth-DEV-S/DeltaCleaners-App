package com.example.deltacleaners.admin.data.repository

import com.example.deltacleaners.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getDashboardStats(): Map<String, Any> {
        return try {
            val bookings = firestore.collection("bookings").get().await()
            val users = firestore.collection("users").get().await()
            val cleaners = firestore.collection("cleaners").get().await()
            
            val bookingDocs = bookings.documents
            val totalRevenue = bookingDocs.filter { it.getString("status") == "Completed" }
                .sumOf { it.getString("price")?.toDoubleOrNull() ?: 0.0 }
            
            mapOf(
                "totalBookings" to bookingDocs.size,
                "pendingBookings" to bookingDocs.count { it.getString("status") == "Pending" },
                "completedBookings" to bookingDocs.count { it.getString("status") == "Completed" },
                "cancelledBookings" to bookingDocs.count { it.getString("status") == "Cancelled" },
                "totalRevenue" to totalRevenue,
                "totalUsers" to users.size(),
                "activeCleaners" to cleaners.size()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error fetching dashboard stats")
            emptyMap()
        }
    }

    suspend fun getAllCleaners(): List<Cleaner> {
        return try {
            firestore.collection("cleaners").get().await().documents.mapNotNull { doc ->
                try {
                    doc.toObject(Cleaner::class.java)
                } catch (e: Exception) {
                    Timber.e(e, "Error deserializing cleaner ${doc.id}")
                    null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching cleaners")
            emptyList()
        }
    }

    suspend fun saveCleaner(cleaner: Cleaner): Result<Unit> {
        return try {
            val id = cleaner.cleanerId.ifEmpty { firestore.collection("cleaners").document().id }
            val finalCleaner = cleaner.copy(cleanerId = id)
            firestore.collection("cleaners").document(id).set(finalCleaner).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllBookings(): List<Booking> {
        return try {
            firestore.collection("bookings")
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Booking::class.java)
                    } catch (e: Exception) {
                        Timber.e(e, "Error deserializing booking ${doc.id}")
                        null
                    }
                }
                .sortedByDescending { it.createdAt as? Comparable<Any?> }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching all bookings")
            emptyList()
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String, cleanerId: String? = null): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to status)
            cleanerId?.let { updates["cleanerId"] = it }
            firestore.collection("bookings").document(bookingId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteService(serviceId: String): Result<Unit> {
        return try {
            firestore.collection("services").document(serviceId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
