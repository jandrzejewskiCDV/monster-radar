package pl.cdv.monsterradar.repository

import com.google.android.gms.maps.model.LatLng
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import pl.cdv.monsterradar.domain.DistanceCalculator
import pl.cdv.monsterradar.domain.MonsterSpawnService

class MonsterRepositoryTest {

    private lateinit var repository: MonsterRepository
    private val testPlayerPos = LatLng(52.2297, 21.0122)
    private val fixedSpawnPos = LatLng(52.2300, 21.0125)

    @Before
    fun setup() {
        repository = MonsterRepository()
        
        mockkObject(MonsterSpawnService)
        every { MonsterSpawnService.calculateSpawnPosition(any()) } returns fixedSpawnPos
        
        mockkObject(DistanceCalculator)
        every { DistanceCalculator.distanceBetween(any(), any()) } returns 100f
    }

    @After
    fun teardown() {
        unmockkObject(MonsterSpawnService)
        unmockkObject(DistanceCalculator)
    }

    @Test
    fun `spawnMonster adds one monster to the list`() = runTest {
        repository.spawnMonster(testPlayerPos)
        
        val monsters = repository.monsters.first()
        assertEquals(1, monsters.size)
    }

    @Test
    fun `spawnMonster creates monster with correct properties`() = runTest {
        repository.spawnMonster(testPlayerPos)
        
        val monster = repository.monsters.first().first()
        assertEquals("Monster", monster.name)
        assertEquals(fixedSpawnPos, monster.position)
        assertEquals(10.0f, monster.speedMetersPerSecond)
        assertNotNull(monster.id)
    }

    @Test
    fun `multiple spawnMonster calls add multiple monsters`() = runTest {
        repository.spawnMonster(testPlayerPos)
        repository.spawnMonster(testPlayerPos)
        repository.spawnMonster(testPlayerPos)
        
        val monsters = repository.monsters.first()
        assertEquals(3, monsters.size)
    }

    @Test
    fun `spawnWave with default count adds 3 monsters`() = runTest {
        repository.spawnWave(testPlayerPos)
        
        val monsters = repository.monsters.first()
        assertEquals(3, monsters.size)
    }

    @Test
    fun `spawnWave with custom count adds correct number of monsters`() = runTest {
        repository.spawnWave(testPlayerPos, count = 5)
        
        val monsters = repository.monsters.first()
        assertEquals(5, monsters.size)
    }

    @Test
    fun `spawnWave with zero count adds no monsters`() = runTest {
        repository.spawnWave(testPlayerPos, count = 0)
        
        val monsters = repository.monsters.first()
        assertEquals(0, monsters.size)
    }

    @Test
    fun `clearAll removes all monsters`() = runTest {
        repository.spawnWave(testPlayerPos, count = 5)
        repository.clearAll()
        
        val monsters = repository.monsters.first()
        assertEquals(0, monsters.size)
    }

    @Test
    fun `clearAll on empty list does nothing`() = runTest {
        repository.clearAll()
        
        val monsters = repository.monsters.first()
        assertEquals(0, monsters.size)
    }

    @Test
    fun `isAnyMonsterWithinDistance returns false when no monsters`() {
        val result = repository.isAnyMonsterWithinDistance(testPlayerPos, 50f)
        assertFalse(result)
    }

    @Test
    fun `isAnyMonsterWithinDistance returns true when monster is close`() = runTest {
        every { DistanceCalculator.distanceBetween(any(), any()) } returns 30f
        
        repository.spawnMonster(testPlayerPos)
        
        val result = repository.isAnyMonsterWithinDistance(testPlayerPos, 50f)
        assertTrue(result)
    }

    @Test
    fun `isAnyMonsterWithinDistance returns false when monster is far`() = runTest {
        every { DistanceCalculator.distanceBetween(any(), any()) } returns 100f
        
        repository.spawnMonster(testPlayerPos)
        
        val result = repository.isAnyMonsterWithinDistance(testPlayerPos, 50f)
        assertFalse(result)
    }

    @Test
    fun `findNearestMonster returns null when no monsters`() {
        val result = repository.findNearestMonster(testPlayerPos)
        assertNull(result)
    }

    @Test
    fun `findNearestMonster returns monster with distance`() = runTest {
        every { DistanceCalculator.distanceBetween(any(), any()) } returns 75f
        
        repository.spawnMonster(testPlayerPos)
        
        val result = repository.findNearestMonster(testPlayerPos)
        assertNotNull(result)
        assertEquals(75f, result!!.second)
    }

    @Test
    fun `findNearestMonster returns closest monster when multiple exist`() = runTest {
        val pos1 = LatLng(52.2301, 21.0126)
        val pos2 = LatLng(52.2305, 21.0130)
        
        every { MonsterSpawnService.calculateSpawnPosition(any()) } returnsMany listOf(pos1, pos2)
        
        every { DistanceCalculator.distanceBetween(testPlayerPos, pos1) } returns 50f
        every { DistanceCalculator.distanceBetween(testPlayerPos, pos2) } returns 100f
        
        repository.spawnMonster(testPlayerPos)
        repository.spawnMonster(testPlayerPos)
        
        val result = repository.findNearestMonster(testPlayerPos)
        assertNotNull(result)
        assertEquals(50f, result!!.second)
    }
}
