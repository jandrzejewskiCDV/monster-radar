package pl.cdv.monsterradar.monsters

import com.google.android.gms.maps.model.LatLng

data class Monster(
    val id: String,
    val name: String,
    var position: LatLng,
    val speedMetersPerSecond: Float,
    val iconRes: Int
)
