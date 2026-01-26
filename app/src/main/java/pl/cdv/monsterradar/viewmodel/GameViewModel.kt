package pl.cdv.monsterradar.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.cdv.monsterradar.domain.DistanceCalculator
import pl.cdv.monsterradar.model.GameState
import pl.cdv.monsterradar.model.Monster
import pl.cdv.monsterradar.repository.MonsterRepository

class GameViewModel : ViewModel() {

    companion object {
        private const val PLAYER_HIT_DISTANCE_METERS = 5f
        private const val WARNING_DISTANCE_METERS = 67f
        private const val TOAST_INTERVAL_SECONDS = 5
        private const val SPAWN_WAVE_INTERVAL_SECONDS = 10
    }

    private val repository = MonsterRepository()
    private val tickHandler = Handler(Looper.getMainLooper())

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
            //to separate function spawn initial wave
            if (!currentState.monstersSpawned) {
                repository.spawnWave(playerPos)
                newState = newState.copy(monstersSpawned = true)
            }

            //to seperate function called "spawn new waves"
            if (newElapsedSeconds % SPAWN_WAVE_INTERVAL_SECONDS == 0) {
                repository.spawnWave(playerPos)
            }

            //update monster positions
            repository.updatePositions(playerPos, 1f)

            //show warnings
            val showWarning = repository.isAnyMonsterWithinDistance(playerPos, WARNING_DISTANCE_METERS)
            newState = newState.copy(showWarning = showWarning)

            if (newElapsedSeconds % TOAST_INTERVAL_SECONDS == 0) {
                val message = generateZombieStatusMessage(playerPos)
                newState = newState.copy(zombieStatusMessage = message)
            } else {
                newState = newState.copy(zombieStatusMessage = null)
            }
            //this whole thingy

            //detectGameOver
            if (repository.isAnyMonsterWithinDistance(playerPos, PLAYER_HIT_DISTANCE_METERS)) {
                newState = newState.copy(isGameOver = true)
                tickHandler.removeCallbacks(tickRunnable)
            }
        }

        _gameState.value = newState
    }

    private fun generateZombieStatusMessage(playerPos: LatLng): String {
        val monsterList = repository.monsters.value
        val nearestInfo = repository.findNearestMonster(playerPos)

        return if (monsterList.isNotEmpty() && nearestInfo != null) {
            val (nearestMonster, distance) = nearestInfo
            val bearing = DistanceCalculator.bearingBetween(playerPos, nearestMonster.position)
            val direction = DistanceCalculator.bearingToDirection(bearing)
            val distanceString = DistanceCalculator.formatDistance(distance)
            
            "${monsterList.size} zombies approaching! Nearest one is $distanceString away from the $direction."
        } else {
            "No zombies detected nearby. Stay safe!"
        }
    }

    fun generateShareMessage(survivalTime: String): String {
        return "I survived the monster apocalypse for $survivalTime! Can you beat my time?"
    }

    override fun onCleared() {
        super.onCleared()
        tickHandler.removeCallbacks(tickRunnable)
    }
}
