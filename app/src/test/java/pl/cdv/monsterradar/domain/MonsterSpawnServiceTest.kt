package pl.cdv.monsterradar.domain

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertTrue
import org.junit.Test

class MonsterSpawnServiceTest {

    companion object {
        private const val MIN_SPAWN_RADIUS = 0.002
        private const val MAX_SPAWN_RADIUS = 0.004
        private const val TEST_ITERATIONS = 100
    }

    @Test
    fun `calculateSpawnPosition returns position within valid radius range`() {
        val playerPos = LatLng(52.2297, 21.0122) // Warszawa

        repeat(TEST_ITERATIONS) {
            val spawnPos = MonsterSpawnService.calculateSpawnPosition(playerPos)
            
            val deltaLat = spawnPos.latitude - playerPos.latitude
            val deltaLng = spawnPos.longitude - playerPos.longitude

            assertTrue(
                "Latitude offset $deltaLat should be within reasonable range",
                kotlin.math.abs(deltaLat) <= MAX_SPAWN_RADIUS * 2
            )
            assertTrue(
                "Longitude offset $deltaLng should be within reasonable range",
                kotlin.math.abs(deltaLng) <= MAX_SPAWN_RADIUS * 3
            )
            
            assertTrue(
                "Spawn position should be different from player position",
                deltaLat != 0.0 || deltaLng != 0.0
            )
        }
    }

    @Test
    fun `calculateSpawnPosition returns different positions on multiple calls`() {
        val playerPos = LatLng(52.2297, 21.0122)
        
        val positions = (1..10).map { 
            MonsterSpawnService.calculateSpawnPosition(playerPos) 
        }
        
        val uniquePositions = positions.distinctBy { "${it.latitude},${it.longitude}" }
        assertTrue(
            "Should generate at least 2 unique positions out of 10",
            uniquePositions.size >= 2
        )
    }

    @Test
    fun `calculateSpawnPosition works for different player positions`() {
        val positions = listOf(
            LatLng(0.0, 0.0),         // Równik
            LatLng(52.2297, 21.0122), // Warszawa
            LatLng(-33.8688, 151.2093), // Sydney
            LatLng(51.5074, -0.1278)  // Londyn
        )

        positions.forEach { playerPos ->
            val spawnPos = MonsterSpawnService.calculateSpawnPosition(playerPos)
            
            assertTrue(
                "Spawn lat should be different from player lat",
                spawnPos.latitude != playerPos.latitude || spawnPos.longitude != playerPos.longitude
            )
        }
    }
}
