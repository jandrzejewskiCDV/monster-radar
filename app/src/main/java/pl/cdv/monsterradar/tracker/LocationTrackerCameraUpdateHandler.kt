package pl.cdv.monsterradar.tracker

import com.google.android.gms.maps.GoogleMap

class LocationTrackerCameraUpdateHandler(private val locationTrackerSystem: LocationTrackerSystem) : GoogleMap.OnCameraMoveStartedListener{
    override fun onCameraMoveStarted(reason: Int) {
        if(reason != GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE)
            return

        locationTrackerSystem.state = LocationTrackerState.FREE_VIEW
    }
}