package com.example.deltacleaners.data.repository

import com.example.deltacleaners.data.model.Service
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val servicesCollection = firestore.collection("services")

    fun getActiveServices(): Flow<List<Service>> = callbackFlow {
        val listener = servicesCollection
            .whereEqualTo("enabled", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val services = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Service::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(services)
            }
        awaitClose { listener.remove() }
    }
}
