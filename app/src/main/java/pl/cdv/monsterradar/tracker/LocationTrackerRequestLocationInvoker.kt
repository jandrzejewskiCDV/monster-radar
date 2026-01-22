package pl.cdv.monsterradar.tracker

import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import pl.cdv.monsterradar.MainActivity

class LocationTrackerRequestLocationInvoker(
    private val googleMap: GoogleMap,
    private val locationTrackerSystem: LocationTrackerSystem,
    private val fusedLocationClient: FusedLocationProviderClient
){
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationTrackerSystem.state != LocationTrackerState.FOLLOW_USER) {
                return;
            }

            val lastLocation = locationResult.lastLocation ?: return
            val userLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLatLng, MainActivity.DEFAULT_ZOOM_LEVEL)
            googleMap.animateCamera(cameraUpdate)
            locationTrackerSystem.lastLocation = userLatLng
            locationTrackerSystem.initialized = true
        }
    }

    fun startTrackingUser() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            googleMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            println("Failed tracking user")
        }
    }

    fun clear(){
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}