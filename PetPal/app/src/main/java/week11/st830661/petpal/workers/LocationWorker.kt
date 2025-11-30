package week11.st830661.petpal.workers

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.ComponentActivity
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class LocationWorker : ComponentActivity() {
    val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    var currentUserLocation : LatLng = LatLng(0.0, 0.0)


    fun getUserLocation() : LatLng {
        val cancellationTokenSource = CancellationTokenSource()
        lifecycleScope.launch @SuppressLint("MissingPermission") {
            suspendCancellableCoroutine { continuation ->
                fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentUserLocation = LatLng(location.latitude, location.longitude)
                    }
                }
                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        }
        return currentUserLocation
    }

    fun shouldShowLocationPermissionRationale() =
        shouldShowRequestPermissionRationale(
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) || shouldShowRequestPermissionRationale(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    fun areLocationPermissionsGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}