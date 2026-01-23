package pl.cdv.monsterradar.tracker

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class LocationTrackerSystem {
    private val googleMap: GoogleMap
    private val tracker: LocationTrackerRequestLocationInvoker

    private var _state: LocationTrackerState = LocationTrackerState.FOLLOW_USER

    var state: LocationTrackerState
        get() = this._state
        set(value) { this._state = value; }

    private var _lastLocation: LatLng? = null

    var lastLocation: LatLng?
        get() = this._lastLocation
        set(value) { this._lastLocation = value; }

    private var _initialiazed: Boolean = false

    var initialized: Boolean
        get() = this._initialiazed
        set(value) { this._initialiazed = value; }

    constructor(googleMap: GoogleMap, fusedLocationProviderClient: FusedLocationProviderClient){
        this.googleMap = googleMap
        listen()

        tracker = LocationTrackerRequestLocationInvoker(googleMap, this, fusedLocationProviderClient)
        tracker.startTrackingUser()
    }

    private fun listen() {
        googleMap.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE ||
                reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION
            ) {
                lastLocation = googleMap.cameraPosition.target
            }
        }

        googleMap.setOnMyLocationButtonClickListener {
            lastLocation = googleMap.cameraPosition.target
            false
        }
    }

    fun clear() {
        tracker.clear()
    }
}