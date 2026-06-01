package com.example.deltacleaners.data.repository

import com.example.deltacleaners.data.model.Banner
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannerRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val bannersCollection = firestore.collection("banners")

    fun getActiveBanners(): Flow<List<Banner>> = callbackFlow {
        val listener = bannersCollection
            .whereEqualTo("enabled", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error fetching banners")
                    close(error)
                    return@addSnapshotListener
                }
                
                val banners = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Banner::class.java)
                    } catch (e: Exception) {
                        Timber.e(e, "Error deserializing banner ${doc.id}")
                        null
                    }
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                
                Timber.d("Fetched ${banners.size} active banners")
                trySend(banners)
            }
        awaitClose { listener.remove() }
    }
}
