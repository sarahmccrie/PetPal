package week11.st830661.petpal.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

data class LocationData(
    val coords : GeoPoint = GeoPoint(0.0, 0.0),
    val clinicName : String = "",
    val address : String = ""
)