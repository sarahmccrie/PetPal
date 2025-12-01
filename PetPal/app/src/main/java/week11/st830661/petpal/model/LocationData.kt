package week11.st830661.petpal.model

import com.google.android.gms.maps.model.LatLng

data class LocationData(
    val coords : LatLng = LatLng(0.0, 0.0),
    val clinicName : String = "",
    val address : String = ""
)