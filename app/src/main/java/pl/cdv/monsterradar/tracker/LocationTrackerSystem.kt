package pl.cdv.monsterradar.tracker

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap

class LocationTrackerSystem {
    private val googleMap: GoogleMap;
    private val tracker: LocationTrackerRequestLocationInvoker;

    private var _state: LocationTrackerState = LocationTrackerState.FOLLOW_USER;

    var state: LocationTrackerState
        get() = this._state;
        set(value) { this._state = value; }

    constructor(googleMap: GoogleMap, fusedLocationProviderClient: FusedLocationProviderClient){
        this.googleMap = googleMap;
        listen()

        tracker = LocationTrackerRequestLocationInvoker(googleMap, this, fusedLocationProviderClient)
        tracker.startTrackingUser()
    }

    private fun listen(){
        this.googleMap.setOnCameraMoveStartedListener(LocationTrackerCameraUpdateHandler(this))
        this.googleMap.setOnMyLocationButtonClickListener(LocationTrackerMyLocationUpdateHandler(this))
    }

    fun clear() {
        tracker.clear()
    }
}