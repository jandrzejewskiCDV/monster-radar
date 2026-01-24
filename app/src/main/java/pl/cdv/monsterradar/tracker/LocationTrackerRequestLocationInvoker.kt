package pl.cdv.monsterradar.tracker

import android.location.Location
import android.os.Looper
import android.util.Log
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
            val lastLocation = locationResult.lastLocation ?: return
            cacheLocation(lastLocation)
            moveCamera()
        }
    }

    fun cacheLocation(location: Location){
        val userLatLng = LatLng(location.latitude, location.longitude)

        locationTrackerSystem.lastLocation = userLatLng
        locationTrackerSystem.initialized = true

        Log.d("ZOMBIE", "Location update caught: $userLatLng")
    }

    fun moveCamera(){
        if (locationTrackerSystem.state != LocationTrackerState.FOLLOW_USER) {
            return;
        }

        val latLng = locationTrackerSystem.lastLocation ?: return

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, MainActivity.DEFAULT_ZOOM_LEVEL)
        googleMap.animateCamera(cameraUpdate)
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
            Log.d("ZOMBIE", "Failed tracking updates")
        }
    }

    fun clear(){
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("ZOMBIE", "Location tracker will from now on, not receive updates")
    }
}