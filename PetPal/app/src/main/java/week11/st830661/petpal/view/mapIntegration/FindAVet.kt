package week11.st830661.petpal.view.mapIntegration

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.model.LatLng
import week11.st830661.petpal.workers.LocationWorker

@Composable
fun FindAVet(uid : String,
             OnNavigate : (LatLng) -> Unit) {
    val lw = LocationWorker()
    var locationPermissionsGranted by remember {
        mutableStateOf(lw.areLocationPermissionsGranted())
    }

    var shouldShowLocationPermissionRationale by remember {
        mutableStateOf(false)
    }

    var currentUserLocation by remember {
        mutableStateOf(LatLng(0.0, 0.0))
    }

    val requestLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            locationPermissionsGranted = permissions.values.all { it }
            if (locationPermissionsGranted) {
                lw.getUserLocation()
            } else {
                shouldShowLocationPermissionRationale =
                    lw.shouldShowLocationPermissionRationale()
            }
        })

    fun requestLocationPermission() {
        requestLocationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    if(locationPermissionsGranted){
        val location = lw.getUserLocation()
        currentUserLocation = if(!location.equals(LatLng(0.0, 0.0))) location else currentUserLocation
    }


}

@Composable
fun map(){

}