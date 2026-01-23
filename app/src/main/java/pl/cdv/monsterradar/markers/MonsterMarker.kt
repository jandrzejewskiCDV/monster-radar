package pl.cdv.monsterradar.markers

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import pl.cdv.monsterradar.monsters.Monster

class MonsterMarker(
    context: Context,
    googleMap: GoogleMap,
    private val monster: Monster
) {

    private val marker: Marker

    init {
        val sizeInDp = 60
        val sizeInPx = (sizeInDp * context.resources.displayMetrics.density).toInt()

        val imageView = ImageView(context).apply {
            setImageResource(monster.iconRes)
            layoutParams = ViewGroup.LayoutParams(sizeInPx, sizeInPx)
        }

        val options = AdvancedMarkerOptions()
            .position(monster.position)
            .iconView(imageView)

        marker = googleMap.addMarker(options)!!
    }

    fun updatePosition(newPosition: LatLng) {
        monster.position = newPosition
        marker.position = newPosition
    }

    fun removeFromMap() {
        marker.remove()
    }

    companion object {
        fun create(context: Context, map: GoogleMap, monster: Monster): MonsterMarker {
            return MonsterMarker(context, map, monster)
        }
    }
}