package week11.st830661.petpal.view.mapIntegration

import android.Manifest
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MarkerState.Companion.invoke
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import week11.st830661.petpal.model.LocationData
import week11.st830661.petpal.workers.LocationWorker

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun FindAVet(OnNavigate : (LocationData) -> Unit) {
    val context = LocalContext.current
    val lw = remember {LocationWorker(context)}
    val scope = rememberCoroutineScope()
    var currentLocation by remember { mutableStateOf(LocationData())}
    var currentUserLocation by remember {
        mutableStateOf(LatLng(0.0, 0.0))
    }
    var searchQuery by remember { mutableStateOf("") }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentUserLocation, 12f)
    }
    var locationPermissionsGranted by remember {
        mutableStateOf(lw.areLocationPermissionsGranted(context))
    }

    var shouldShowLocationPermissionRationale by remember {
        mutableStateOf(false)
    }

    val requestLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            locationPermissionsGranted = permissions.values.all { it }
            if (locationPermissionsGranted) {
                scope.launch {
                    currentUserLocation = lw.getUserLocation()
                }
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

    LaunchedEffect(locationPermissionsGranted) {
        if (locationPermissionsGranted) {
            val location = lw.getUserLocation()
            if (location != LatLng(0.0, 0.0)) {
                currentUserLocation = location
                cameraPositionState.animate(
                    update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 12f),
                    durationMs = 1000
                )
            }
        } else {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var markerPosition by remember { mutableStateOf<LatLng?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))) {
            // Google Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                markerPosition?.let { position ->
                    Marker(
                        state = MarkerState(position = position),
                        title = "Selected Location"
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .focusable()
                        .onKeyEvent {
                            when {
                                (it.type == KeyEventType.KeyUp && it.key == Key.Enter) -> {
                                    scope.launch {
                                        lw.searchLocation(context, searchQuery)?.let { geoPoint ->
                                            markerPosition = LatLng(
                                                geoPoint.latitude,
                                                geoPoint.longitude)
                                            cameraPositionState.animate(
                                                update = com.google.android.gms.maps.CameraUpdateFactory
                                                    .newLatLngZoom(
                                                        LatLng(geoPoint.latitude,
                                                        geoPoint.longitude), 15f),
                                                durationMs = 1000
                                            )

                                            currentLocation = lw.getLocationData(context, geoPoint)
                                            if(currentLocation.clinicName.isEmpty()) {
                                                currentLocation = currentLocation
                                                    .copy(
                                                        coords = currentLocation.coords,
                                                        clinicName = searchQuery,
                                                        address = currentLocation.address
                                                    )
                                            }
                                        }
                                    }
                                    true
                                }

                                else -> false
                            }
                        },
                    placeholder = { Text("Search location...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        lw.searchLocation(context, searchQuery)?.let { geoPoint ->
                                            val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
                                            markerPosition = latLng
                                            cameraPositionState.animate(
                                                update = com.google.android.gms.maps.CameraUpdateFactory
                                                    .newLatLngZoom(latLng, 15f),
                                                durationMs = 1000
                                            )

                                            currentLocation = lw.getLocationData(context,
                                                GeoPoint(latLng.latitude, latLng.longitude)
                                            )
                                            if(currentLocation.clinicName.isEmpty()) {
                                                currentLocation = currentLocation
                                                    .copy(
                                                        coords = currentLocation.coords,
                                                        clinicName = searchQuery,
                                                        address = currentLocation.address
                                                    )
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text("Go")
                            }
                        }
                    },
                    singleLine = true
                )
            }
        }
        Button(onClick = {
            Log.d("Test", "Current Location:" +
                    "\n\t${currentLocation.clinicName}" +
                    "\n\t${currentLocation.address}" +
                    "\n\t${currentLocation.coords}")
            Log.d("Test", "Current Location after null check:" +
                    "\n\t${currentLocation.clinicName}" +
                    "\n\t${currentLocation.address}" +
                    "\n\t${currentLocation.coords}")
            OnNavigate(currentLocation)
        }) {
            Text(text = "Set Vet Location")
        }
    }
}

@Composable
fun map(){

}