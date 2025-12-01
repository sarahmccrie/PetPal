package week11.st830661.petpal.workers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import week11.st830661.petpal.model.LocationData
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.annotation.UiContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
//import com.google.android.libraries.places.api.Places
//import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationWorker (context : Context) : ComponentActivity() {
    val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var currentUserLocation : LatLng = LatLng(0.0, 0.0)


    suspend fun getUserLocation() : LatLng = suspendCancellableCoroutine { continuation ->
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                currentUserLocation = latLng
                continuation.resume(latLng) // Resume with the location
            } else {
                continuation.resume(LatLng(0.0, 0.0)) // Resume with default if null
            }
        }.addOnFailureListener { exception ->
            continuation.resume(LatLng(0.0, 0.0)) // Resume with default on failure
        }

        continuation.invokeOnCancellation {
            cancellationTokenSource.cancel()
        }
    }

    fun shouldShowLocationPermissionRationale() =
        shouldShowRequestPermissionRationale(
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) || shouldShowRequestPermissionRationale(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    fun areLocationPermissionsGranted(context : Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun searchLocation(context: Context, query: String): LatLng? =
        suspendCancellableCoroutine { continuation ->
            val geocoder = Geocoder(context, Locale.getDefault())

            val listener = @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    val result = addresses.firstOrNull()?.let { address ->
                        LatLng(address.latitude, address.longitude)
                    }
                    continuation.resume(result) { cause, _, _ -> onCancellation(cause) }
                }

                override fun onError(errorMessage: String?) {
                    continuation.resume(null) { cause, _, _ -> onCancellation(cause) }
                }
            }

            try {
                geocoder.getFromLocationName(query, 1, listener)
            } catch (e: Exception) {
                continuation.resume(null) { cause, _, _ -> onCancellation(cause) }
            }

            continuation.invokeOnCancellation {
                // Cleanup if needed
            }
        }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getLocationData(context: Context, latLng: LatLng): LocationData = suspendCancellableCoroutine { continuation ->
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            // Modern approach for Android 13+
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
//                    val locationName = address.featureName?.takeIf {
//                        // Filter out if it's just a number
//                        it.toIntOrNull() == null
//                    } ?: address.premises
//                    ?: address.subThoroughfare?.let { num ->
//                        address.thoroughfare?.let { street -> "$num $street" }
//                    }
//                    ?: address.thoroughfare
//                    ?: address.locality
//                    ?: "Unknown Location"
//                    Log.d("Test", "Location Name Options:" +
//                            "\n\tpremises: ${address.premises}" +
//                            "\n\tsubThoroughfare: ${address.subThoroughfare}" +
//                            "\n\tthoroughfare: ${address.thoroughfare}" +
//                            "\n\tlocality: ${address.locality}" +
//                            "\n\tlocality: ${address.subLocality}" +
//                            "\n\tfeatureName: ${address.featureName}" +
//                            "\n\tadminArea: ${address.adminArea}" +
//                            "\n\textras: ${address.extras}")
                    val locationData = LocationData(
                        latLng,
                        address.premises ?: "",
                        address.getAddressLine(0) ?: "",
                    )
                    continuation.resume(locationData)
                } else {
                    continuation.resume(LocationData())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            continuation.resume(LocationData())
        }

        continuation.invokeOnCancellation {
            // Handle cancellation if needed
        }
    }
//    suspend fun getLocationData(context: Context, latLng: LatLng): LocationData = withContext(Dispatchers.IO) {
//        try {
//            val geocoder = Geocoder(context, Locale.getDefault())
//
//            // Use the deprecated method for all Android versions
//            // It still works fine and avoids the compiler bug
//            @Suppress("DEPRECATION")
//            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
//
//            if (!addresses.isNullOrEmpty()) {
//                val address = addresses[0]
//                LocationData(
//                    latLng,
//                    address.featureName ?: address.thoroughfare ?: "Unknown Location",
//                    address.getAddressLine(0) ?: ""
//                )
//            } else {
//                LocationData()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            LocationData()
//        }
//    }
}

private fun onCancellation(cause: Throwable) {}