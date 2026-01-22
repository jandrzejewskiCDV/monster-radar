package pl.cdv.monsterradar.monsters

import android.location.Location
import com.google.android.gms.maps.model.LatLng

class MonsterMovementController {

    fun moveMonsterTowards(
        monster: Monster,
        target: LatLng,
        deltaTimeSeconds: Float
    ): LatLng {

        val distanceToMove =
            monster.speedMetersPerSecond * deltaTimeSeconds

        return monster.position.moveTowards(target, distanceToMove.toDouble())
    }

    fun LatLng.moveTowards(
        target: LatLng,
        distanceMeters: Double
    ): LatLng {
        val results = FloatArray(1)
        Location.distanceBetween(
            latitude, longitude,
            target.latitude, target.longitude,
            results
        )

        val totalDistance = results[0]
        if (totalDistance == 0f) return this

        val fraction = distanceMeters / totalDistance

        return LatLng(
            latitude + (target.latitude - latitude) * fraction,
            longitude + (target.longitude - longitude) * fraction
        )
    }
}