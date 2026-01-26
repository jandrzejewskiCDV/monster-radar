package pl.cdv.monsterradar.repository

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.cdv.monsterradar.R
import pl.cdv.monsterradar.domain.DistanceCalculator
import pl.cdv.monsterradar.domain.MonsterMovementController
import pl.cdv.monsterradar.domain.MonsterSpawnService
import pl.cdv.monsterradar.model.Monster
import java.util.UUID

class MonsterRepository {

    private val movementController = MonsterMovementController()
    
    private val _monsters = MutableStateFlow<List<Monster>>(emptyList())
    val monsters: StateFlow<List<Monster>> = _monsters.asStateFlow()

    fun spawnMonster(playerPos: LatLng) {
        val spawnPosition = MonsterSpawnService.calculateSpawnPosition(playerPos)
        val newMonster = Monster(
            id = UUID.randomUUID().toString(),
            name = "Monster",
            position = spawnPosition,
            speedMetersPerSecond = 10.0f,
            iconRes = R.drawable.monster_anim
        )
        _monsters.value += newMonster
    }

    fun spawnWave(playerPos: LatLng, count: Int = 3) {
        repeat(count) { spawnMonster(playerPos) }
    }

    fun updatePositions(target: LatLng, deltaTime: Float) {
        _monsters.value = _monsters.value.map { monster ->
            monster.copy(
                position = movementController.moveMonsterTowards(monster, target, deltaTime)
            )
        }
    }

    fun clearAll() {
        _monsters.value = emptyList()
    }

    fun isAnyMonsterWithinDistance(playerPos: LatLng, distanceMeters: Float): Boolean {
        return _monsters.value.any { monster ->
            DistanceCalculator.distanceBetween(playerPos, monster.position) <= distanceMeters
        }
    }

    fun findNearestMonster(playerPos: LatLng): Pair<Monster, Float>? {
        return _monsters.value
            .map { monster -> monster to DistanceCalculator.distanceBetween(playerPos, monster.position) }
            .minByOrNull { it.second }
    }
}
