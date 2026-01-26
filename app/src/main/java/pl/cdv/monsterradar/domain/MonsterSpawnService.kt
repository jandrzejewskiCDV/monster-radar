package pl.cdv.monsterradar.domain

import com.google.android.gms.maps.model.LatLng
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object MonsterSpawnService {

    private const val MIN_SPAWN_RADIUS = 0.002
    private const val MAX_SPAWN_RADIUS = 0.004

    fun calculateSpawnPosition(playerPos: LatLng): LatLng {
        val theta = Random.nextDouble(0.0, 2 * PI)

        val minSq = MIN_SPAWN_RADIUS * MIN_SPAWN_RADIUS
        val maxSq = MAX_SPAWN_RADIUS * MAX_SPAWN_RADIUS
        val r = sqrt(Random.nextDouble(minSq, maxSq))

        val offsetLat = r * cos(theta)
        val offsetLng = (r * sin(theta)) / cos(Math.toRadians(playerPos.latitude))

        return LatLng(
            playerPos.latitude + offsetLat,
            playerPos.longitude + offsetLng
        )
    }
}
