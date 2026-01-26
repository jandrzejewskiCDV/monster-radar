package pl.cdv.monsterradar.location

import android.annotation.SuppressLint
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

class PlayerLocationTracker(
    private val googleMap: GoogleMap,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val defaultZoomLevel: Float = 18f
) {
    var lastLocation: LatLng? = null
        private set

    var isInitialized: Boolean = false
        private set

    var cameraMode: CameraFollowMode = CameraFollowMode.FOLLOW_PLAYER
        private set

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                updateLocation(location)
            }
        }
    }

    init {
        setupMapListeners()
    }

    private fun setupMapListeners() {
        googleMap.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                cameraMode = CameraFollowMode.FREE_VIEW
            }
        }

        googleMap.setOnMyLocationButtonClickListener {
            cameraMode = CameraFollowMode.FOLLOW_PLAYER
            false
        }
    }

    private fun updateLocation(location: Location) {
        lastLocation = LatLng(location.latitude, location.longitude)
        isInitialized = true

        if (cameraMode == CameraFollowMode.FOLLOW_PLAYER) {
            animateCameraToPlayer()
        }
    }

    private fun animateCameraToPlayer() {
        lastLocation?.let { latLng ->
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, defaultZoomLevel)
            googleMap.animateCamera(cameraUpdate)
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                lastLocation = LatLng(it.latitude, it.longitude)
                isInitialized = true
            }
        }

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
            Log.e("PlayerLocationTracker", "Failed to start tracking: ${e.message}")
        }
    }

    fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
