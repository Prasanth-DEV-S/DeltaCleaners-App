package com.example.deltacleaners.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class LocationData(
    val street: String,
    val city: String,
    val latitude: Double,
    val longitude: Double
)

@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val availability = GoogleApiAvailability.getInstance()
        val resultCode = availability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    private fun hasLocationPermissions(): Boolean {
        val fineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        val coarseLocation = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData? {
        if (!isGooglePlayServicesAvailable()) {
            Timber.w("Google Play Services not available")
            return null
        }
        
        if (!hasLocationPermissions()) {
            Timber.w("Location permissions not granted")
            return null
        }

        return try {
            // 1. Try to get last known location first (fastest and most reliable fallback)
            val lastLocation = try {
                fusedLocationClient.lastLocation.await()
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException while getting last location: Check SHA-1 registration in Firebase Console for package ${context.packageName}")
                null
            } catch (e: Exception) {
                Timber.e(e, "Error getting last location")
                null
            }

            if (lastLocation != null && isLocationRecent(lastLocation)) {
                return getAddressFromLocation(lastLocation)
            }

            // 2. Try to get fresh location with timeout
            val cancellationTokenSource = CancellationTokenSource()
            val location = try {
                withTimeoutOrNull(10000L) { // 10 seconds timeout
                    try {
                        val request = CurrentLocationRequest.Builder()
                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                            .setDurationMillis(10000)
                            .build()
                        
                        fusedLocationClient.getCurrentLocation(
                            request,
                            cancellationTokenSource.token
                        ).await()
                    } catch (e: SecurityException) {
                        Timber.e(e, "SecurityException: GMS failed to identify calling package. Ensure SHA-1 is registered in Firebase.")
                        null
                    } catch (e: Exception) {
                        Timber.e(e, "Error getting current location")
                        null
                    } finally {
                        cancellationTokenSource.cancel()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Timeout or exception while fetching fresh location")
                null
            }

            location?.let {
                getAddressFromLocation(it)
            } ?: lastLocation?.let {
                // Fallback to last location if fresh one failed or timed out
                getAddressFromLocation(it)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching location")
            null
        }
    }

    private fun isLocationRecent(location: android.location.Location): Boolean {
        val age = System.currentTimeMillis() - location.time
        return age < 5 * 60 * 1000 // 5 minutes
    }

    private suspend fun getAddressFromLocation(location: android.location.Location): LocationData? {
        if (!Geocoder.isPresent()) {
            Timber.w("Geocoder is not present on this device")
            return LocationData(
                street = "",
                city = "Unknown City",
                latitude = location.latitude,
                longitude = location.longitude
            )
        }

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        ) { addresses ->
                            if (addresses.isNotEmpty()) {
                                val address = addresses[0]
                                continuation.resume(
                                    LocationData(
                                        street = address.thoroughfare ?: address.subLocality ?: address.featureName ?: "",
                                        city = address.locality ?: address.subAdminArea ?: "Unknown City",
                                        latitude = location.latitude,
                                        longitude = location.longitude
                                    )
                                )
                            } else {
                                continuation.resume(
                                    LocationData(
                                        street = "",
                                        city = "Unknown City",
                                        latitude = location.latitude,
                                        longitude = location.longitude
                                    )
                                )
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        LocationData(
                            street = address.thoroughfare ?: address.subLocality ?: address.featureName ?: "",
                            city = address.locality ?: address.subAdminArea ?: "Unknown City",
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    } else {
                        LocationData(
                            street = "",
                            city = "Unknown City",
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    }
                }
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException while geocoding: Unknown calling package issue")
                LocationData(
                    street = "",
                    city = "Unknown City",
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } catch (e: Exception) {
                Timber.e(e, "Error geocoding location")
                LocationData(
                    street = "",
                    city = "Unknown City",
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        }
    }
}
