package com.example.deltacleaners.data.repository

import com.example.deltacleaners.data.model.Review
import com.example.deltacleaners.data.model.Service
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val reviewsCollection = firestore.collection("reviews")
    private val servicesCollection = firestore.collection("services")

    suspend fun submitReview(review: Review): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                // 1. Save the review
                val reviewRef = reviewsCollection.document(review.reviewId)
                transaction.set(reviewRef, review)

                // 2. Update service aggregate rating (Denormalization)
                if (review.serviceId.isNotEmpty()) {
                    val serviceRef = servicesCollection.document(review.serviceId)
                    val serviceSnapshot = transaction.get(serviceRef)
                    
                    if (serviceSnapshot.exists()) {
                        val currentReviews = serviceSnapshot.getLong("totalReviews") ?: 0
                        val currentRating = serviceSnapshot.getDouble("rating") ?: 0.0
                        
                        val newCount = currentReviews + 1
                        val newRating = ((currentRating * currentReviews) + review.rating) / newCount
                        
                        transaction.update(serviceRef, mapOf(
                            "totalReviews" to newCount,
                            "rating" to newRating
                        ))
                    }
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error submitting review and updating stats")
            Result.failure(e)
        }
    }

    /**
     * Get live stream of reviews for a specific service
     */
    fun getServiceReviewsFlow(serviceId: String): Flow<List<Review>> = callbackFlow {
        val listener = reviewsCollection
            .whereEqualTo("serviceId", serviceId)
            // .orderBy("createdAt", Query.Direction.DESCENDING) // Requires composite index
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error in service reviews snapshot listener")
                    close(error)
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Review::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }?.sortedByDescending { it.createdAt as? Comparable<Any?> } ?: emptyList()
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getServiceReviews(serviceId: String): Result<List<Review>> {
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("serviceId", serviceId)
                // .orderBy("createdAt", Query.Direction.DESCENDING) // Requires composite index
                .get()
                .await()
            val reviews = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Review::class.java)
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.createdAt as? Comparable<Any?> }
            Result.success(reviews)
        } catch (e: Exception) {
            Timber.e(e, "Error getting reviews for service: $serviceId")
            Result.failure(e)
        }
    }
}
