package pl.cdv.monsterradar.tracker

import com.google.android.gms.maps.GoogleMap

class LocationTrackerMyLocationUpdateHandler(val locationTrackerSystem: LocationTrackerSystem) : GoogleMap.OnMyLocationButtonClickListener{
    override fun onMyLocationButtonClick(): Boolean {
        locationTrackerSystem.state = LocationTrackerState.FOLLOW_USER
        return false
    }
}