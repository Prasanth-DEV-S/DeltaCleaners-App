package com.example.deltacleaners.data.repository

import com.example.deltacleaners.data.model.AppConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getAppConfig(): AppConfig? {
        return try {
            firestore.collection("app_config")
                .document("version")
                .get()
                .await()
                .toObject(AppConfig::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching app config")
            null
        }
    }
}
