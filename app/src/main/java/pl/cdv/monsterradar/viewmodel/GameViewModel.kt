package pl.cdv.monsterradar.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.cdv.monsterradar.R
import pl.cdv.monsterradar.domain.DistanceCalculator
import pl.cdv.monsterradar.model.GameState
import pl.cdv.monsterradar.model.Monster
import pl.cdv.monsterradar.repository.MonsterRepository
import pl.cdv.monsterradar.util.ResourceProvider

class GameViewModel : ViewModel() {

    private val repository = MonsterRepository()
    private val tickHandler = Handler(Looper.getMainLooper())
    private var resourceProvider: ResourceProvider? = null

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    val monsters: StateFlow<List<Monster>> = repository.monsters

    private var playerLocationProvider: (() -> LatLng?)? = null

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (_gameState.value.isGameOver)
                return
            
            onTick()
            tickHandler.postDelayed(this, 1000)
        }
    }

    fun initResources(provider: ResourceProvider) {
        this.resourceProvider = provider
    }

    fun setPlayerLocationProvider(provider: () -> LatLng?) {
        playerLocationProvider = provider
    }

    fun startGame() {
        tickHandler.removeCallbacks(tickRunnable)
        tickHandler.post(tickRunnable)
    }

    fun pauseGame() {
        tickHandler.removeCallbacks(tickRunnable)
    }

    fun resetGame() {
        tickHandler.removeCallbacks(tickRunnable)
        repository.clearAll()
        _gameState.value = GameState()
        tickHandler.post(tickRunnable)
    }

    private fun onTick() {
        val currentState = _gameState.value
        val newElapsedSeconds = currentState.elapsedSeconds + 1
        val playerPos = playerLocationProvider?.invoke()

        var newState = currentState.copy(elapsedSeconds = newElapsedSeconds)

        if (playerPos != null) {
            newState = spawnInitialWave(playerPos, newState)
            spawnNewWaves(playerPos, newElapsedSeconds)
            updateMonsterPositions(playerPos)
            newState = showWarnings(playerPos, newElapsedSeconds, newState)
            newState = detectGameOver(playerPos, newState)
        }

        _gameState.value = newState
    }

    private fun spawnInitialWave(playerPos: LatLng, state: GameState): GameState {
        if (!state.monstersSpawned) {
            repository.spawnWave(playerPos)
            return state.copy(monstersSpawned = true)
        }
        return state
    }

    private fun spawnNewWaves(playerPos: LatLng, elapsedSeconds: Int) {
        val interval = resourceProvider?.getInteger(R.integer.spawn_wave_interval_seconds) ?: 10
        if (elapsedSeconds % interval == 0) {
            repository.spawnWave(playerPos)
        }
    }

    private fun updateMonsterPositions(playerPos: LatLng) {
        repository.updatePositions(playerPos, 1f)
    }

    private fun showWarnings(playerPos: LatLng, elapsedSeconds: Int, state: GameState): GameState {
        val warningDist = resourceProvider?.getFloat(R.dimen.warning_distance) ?: 67f
        val toastInterval = resourceProvider?.getInteger(R.integer.toast_interval_seconds) ?: 5
        
        val showWarning = repository.isAnyMonsterWithinDistance(playerPos, warningDist)
        val message = if (elapsedSeconds % toastInterval == 0) {
            generateZombieStatusMessage(playerPos)
        } else {
            null
        }
        return state.copy(showWarning = showWarning, zombieStatusMessage = message)
    }

    private fun detectGameOver(playerPos: LatLng, state: GameState): GameState {
        val hitDist = resourceProvider?.getFloat(R.dimen.player_hit_distance) ?: 5f
        if (repository.isAnyMonsterWithinDistance(playerPos, hitDist)) {
            tickHandler.removeCallbacks(tickRunnable)
            return state.copy(isGameOver = true)
        }
        return state
    }
    
    private fun generateZombieStatusMessage(playerPos: LatLng): String {
        val monsterList = repository.monsters.value
        val nearestInfo = repository.findNearestMonster(playerPos)

        return if (monsterList.isNotEmpty() && nearestInfo != null) {
            val (nearestMonster, distance) = nearestInfo
            val bearing = DistanceCalculator.bearingBetween(playerPos, nearestMonster.position)
            val direction = DistanceCalculator.bearingToDirection(bearing)
            val distanceString = DistanceCalculator.formatDistance(distance)
            
            resourceProvider?.getString(
                R.string.zombie_status_message, 
                monsterList.size, 
                distanceString, 
                direction
            ) ?: ""
        } else {
            resourceProvider?.getString(R.string.no_zombies_message) ?: ""
        }
    }

    fun generateShareMessage(survivalTime: String): String {
        return resourceProvider?.getString(R.string.share_message, survivalTime) ?: ""
    }

    override fun onCleared() {
        super.onCleared()
        tickHandler.removeCallbacks(tickRunnable)
    }
}
