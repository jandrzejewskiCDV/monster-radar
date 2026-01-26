package pl.cdv.monsterradar.domain

import android.location.Location
import com.google.android.gms.maps.model.LatLng

object DistanceCalculator {

    fun distanceBetween(from: LatLng, to: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
            results
        )
        return results[0]
    }

    fun bearingBetween(from: LatLng, to: LatLng): Float {
        val fromLocation = Location("").apply {
            latitude = from.latitude
            longitude = from.longitude
        }
        val toLocation = Location("").apply {
            latitude = to.latitude
            longitude = to.longitude
        }
        return fromLocation.bearingTo(toLocation)
    }

    fun bearingToDirection(bearing: Float): String {
        val normalizedBearing = (bearing + 360) % 360
        return when {
            normalizedBearing in 337.5..360.0 || normalizedBearing in 0.0..22.5 -> "North"
            normalizedBearing in 22.5..67.5 -> "North-East"
            normalizedBearing in 67.5..112.5 -> "East"
            normalizedBearing in 112.5..157.5 -> "South-East"
            normalizedBearing in 157.5..202.5 -> "South"
            normalizedBearing in 202.5..247.5 -> "South-West"
            normalizedBearing in 247.5..292.5 -> "West"
            normalizedBearing in 292.5..337.5 -> "North-West"
            else -> "Unknown Direction"
        }
    }

    fun formatDistance(distanceMeters: Float): String {
        return if (distanceMeters < 1000) {
            "${distanceMeters.toInt()} meters"
        } else {
            "${"%.1f".format(distanceMeters / 1000)} km"
        }
    }
}
