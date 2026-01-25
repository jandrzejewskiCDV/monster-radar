package pl.cdv.monsterradar.markers

import android.content.Context
import android.graphics.drawable.AnimationDrawable
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
            // Set the resource (can be a static PNG or our new monster_anim XML)
            setImageResource(monster.iconRes)
            layoutParams = ViewGroup.LayoutParams(sizeInPx, sizeInPx)

            // If the drawable is an animation, start it
            post {
                (drawable as? AnimationDrawable)?.start()
            }
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
