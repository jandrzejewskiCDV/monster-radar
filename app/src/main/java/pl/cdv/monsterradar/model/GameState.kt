package pl.cdv.monsterradar.model

data class GameState(
    val elapsedSeconds: Int = 0,
    val isGameOver: Boolean = false,
    val monstersSpawned: Boolean = false,
    val showWarning: Boolean = false,
    val zombieStatusMessage: String? = null
) {
    val formattedTime: String
        get() = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)
}
