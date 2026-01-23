package pl.cdv.monsterradar.viewmodels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import pl.cdv.monsterradar.R
import pl.cdv.monsterradar.monsters.Monster
import pl.cdv.monsterradar.monsters.MonsterMovementController
import java.util.UUID
import kotlin.random.Random

class MonsterViewModel : ViewModel() {

    private val movementController = MonsterMovementController()

    private val _monsters = MutableLiveData<List<Monster>>(emptyList())
    val monsters: LiveData<List<Monster>> = _monsters

    fun updateMonsters(target: LatLng?, deltaTime: Float) {
        if (target == null) return
        _monsters.value = _monsters.value?.map { monster ->
            monster.copy(
                position = movementController.moveMonsterTowards(
                    monster,
                    target,
                    deltaTime
                )
            )
        }
    }

    fun addNewMonster(
        name: String,
        spawnPosition: LatLng,
        speed: Float,
        iconRes: Int
    ) {
        val newMonster = Monster(
            id = UUID.randomUUID().toString(),
            name = name,
            position = spawnPosition,
            speedMetersPerSecond = speed,
            iconRes = iconRes
        )

        _monsters.value = _monsters.value!! + newMonster
    }

    fun spawnMonsterNearPlayer(playerPos: LatLng) {
        val randomOffset = Random.nextDouble(-0.004, 0.004)
        val spawn = LatLng(
            playerPos.latitude + randomOffset,
            playerPos.longitude + randomOffset
        )

        addNewMonster(
            name = "Monster",
            spawnPosition = spawn,
            speed = 10.0f,
            iconRes = R.drawable.monster
        )
    }

    fun isMonsterNearPlayer(
        playerPos: LatLng,
        warningDistanceMeters: Float
    ): Boolean {
        val results = FloatArray(1)

        return _monsters.value?.any { monster ->
            Location.distanceBetween(
                playerPos.latitude,
                playerPos.longitude,
                monster.position.latitude,
                monster.position.longitude,
                results
            )
            results[0] <= warningDistanceMeters
        } ?: false
    }

    fun isMonsterTouchingPlayer(
        playerPos: LatLng,
        hitDistanceMeters: Float
    ): Boolean {
        val results = FloatArray(1)

        return _monsters.value?.any { monster ->
            Location.distanceBetween(
                playerPos.latitude,
                playerPos.longitude,
                monster.position.latitude,
                monster.position.longitude,
                results
            )
            results[0] <= hitDistanceMeters
        } ?: false
    }

    fun clearMonsters() {
        _monsters.value = emptyList()
    }

}