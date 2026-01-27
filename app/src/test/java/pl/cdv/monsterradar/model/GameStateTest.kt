package pl.cdv.monsterradar.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GameStateTest {

    @Test
    fun `formattedTime returns 00 colon 00 for 0 seconds`() {
        val state = GameState(elapsedSeconds = 0)
        assertEquals("00:00", state.formattedTime)
    }

    @Test
    fun `formattedTime returns 00 colon 01 for 1 second`() {
        val state = GameState(elapsedSeconds = 1)
        assertEquals("00:01", state.formattedTime)
    }

    @Test
    fun `formattedTime returns 00 colon 59 for 59 seconds`() {
        val state = GameState(elapsedSeconds = 59)
        assertEquals("00:59", state.formattedTime)
    }

    @Test
    fun `formattedTime returns 01 colon 00 for 60 seconds`() {
        val state = GameState(elapsedSeconds = 60)
        assertEquals("01:00", state.formattedTime)
    }

    @Test
    fun `formattedTime returns 01 colon 30 for 90 seconds`() {
        val state = GameState(elapsedSeconds = 90)
        assertEquals("01:30", state.formattedTime)
    }

    @Test
    fun `formattedTime returns 10 colon 00 for 600 seconds`() {
        val state = GameState(elapsedSeconds = 600)
        assertEquals("10:00", state.formattedTime)
    }

    @Test
    fun `formattedTime handles over an hour correctly`() {
        // 3661 seconds = 61 minutes 1 second = 61:01
        val state = GameState(elapsedSeconds = 3661)
        assertEquals("61:01", state.formattedTime)
    }

    @Test
    fun `formattedTime handles large values`() {
        // 5999 seconds = 99 minutes 59 seconds
        val state = GameState(elapsedSeconds = 5999)
        assertEquals("99:59", state.formattedTime)
    }

    @Test
    fun `default GameState has zero elapsed seconds`() {
        val state = GameState()
        assertEquals(0, state.elapsedSeconds)
    }

    @Test
    fun `default GameState is not game over`() {
        val state = GameState()
        assertEquals(false, state.isGameOver)
    }

    @Test
    fun `default GameState has no monsters spawned`() {
        val state = GameState()
        assertEquals(false, state.monstersSpawned)
    }

    @Test
    fun `default GameState has no warning`() {
        val state = GameState()
        assertEquals(false, state.showWarning)
    }

    @Test
    fun `default GameState has null zombie status message`() {
        val state = GameState()
        assertEquals(null, state.zombieStatusMessage)
    }
}
