package pl.cdv.monsterradar.ui.renderer

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import pl.cdv.monsterradar.markers.MonsterMarker
import pl.cdv.monsterradar.model.Monster

class MonsterMapRenderer(
    private val context: Context,
    private val googleMap: GoogleMap
) {
    private val monsterMarkers = mutableMapOf<String, MonsterMarker>()

    fun render(monsters: List<Monster>) {
        val activeIds = monsters.map { it.id }.toSet()

        // Remove stale markers
        val iterator = monsterMarkers.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key !in activeIds) {
                entry.value.removeFromMap()
                iterator.remove()
            }
        }

        monsters.forEach { monster ->
            val existingMarker = monsterMarkers[monster.id]
            if (existingMarker == null) {
                monsterMarkers[monster.id] = MonsterMarker(context, googleMap, monster)
            } else {
                existingMarker.updatePosition(monster.position)
            }
        }
    }

    fun clear() {
        monsterMarkers.values.forEach { it.removeFromMap() }
        monsterMarkers.clear()
    }
}
