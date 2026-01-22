package pl.cdv.monsterradar.viewmodels

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

    fun updateMonsters(target: LatLng, deltaTime: Float) {
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
        val randomOffset = Random.nextDouble(-0.001, 0.001)
        val spawn = LatLng(
            playerPos.latitude + randomOffset,
            playerPos.longitude + randomOffset
        )

        addNewMonster(
            name = "Chaser",
            spawnPosition = spawn,
            speed = 2.0f,
            iconRes = R.drawable.monster
        )
    }

}