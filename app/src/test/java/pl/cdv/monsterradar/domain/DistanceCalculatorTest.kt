package pl.cdv.monsterradar.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceCalculatorTest {
    @Test
    fun `bearingToDirection returns North for 0 degrees`() {
        assertEquals("North", DistanceCalculator.bearingToDirection(0f))
    }

    @Test
    fun `bearingToDirection returns North for 360 degrees`() {
        assertEquals("North", DistanceCalculator.bearingToDirection(360f))
    }

    @Test
    fun `bearingToDirection returns North for small positive angle`() {
        assertEquals("North", DistanceCalculator.bearingToDirection(10f))
    }

    @Test
    fun `bearingToDirection returns North for angle close to 360`() {
        assertEquals("North", DistanceCalculator.bearingToDirection(350f))
    }

    @Test
    fun `bearingToDirection returns North-East for 45 degrees`() {
        assertEquals("North-East", DistanceCalculator.bearingToDirection(45f))
    }

    @Test
    fun `bearingToDirection returns East for 90 degrees`() {
        assertEquals("East", DistanceCalculator.bearingToDirection(90f))
    }

    @Test
    fun `bearingToDirection returns South-East for 135 degrees`() {
        assertEquals("South-East", DistanceCalculator.bearingToDirection(135f))
    }

    @Test
    fun `bearingToDirection returns South for 180 degrees`() {
        assertEquals("South", DistanceCalculator.bearingToDirection(180f))
    }

    @Test
    fun `bearingToDirection returns South-West for 225 degrees`() {
        assertEquals("South-West", DistanceCalculator.bearingToDirection(225f))
    }

    @Test
    fun `bearingToDirection returns West for 270 degrees`() {
        assertEquals("West", DistanceCalculator.bearingToDirection(270f))
    }

    @Test
    fun `bearingToDirection returns North-West for 315 degrees`() {
        assertEquals("North-West", DistanceCalculator.bearingToDirection(315f))
    }

    @Test
    fun `bearingToDirection handles negative bearing correctly`() {
        // -45 degrees should normalize to 315 degrees = North-West
        assertEquals("North-West", DistanceCalculator.bearingToDirection(-45f))
    }

    @Test
    fun `bearingToDirection handles large negative bearing correctly`() {
        // -90 degrees should normalize to 270 degrees = West
        assertEquals("West", DistanceCalculator.bearingToDirection(-90f))
    }

    // ==================== formatDistance Tests ====================

    @Test
    fun `formatDistance returns meters for distance less than 1000`() {
        assertEquals("500 meters", DistanceCalculator.formatDistance(500f))
    }

    @Test
    fun `formatDistance returns 0 meters for zero distance`() {
        assertEquals("0 meters", DistanceCalculator.formatDistance(0f))
    }

    @Test
    fun `formatDistance returns 999 meters for 999`() {
        assertEquals("999 meters", DistanceCalculator.formatDistance(999f))
    }

    @Test
    fun `formatDistance returns km for distance 1000 or more`() {
        val result = DistanceCalculator.formatDistance(1000f)
        assertTrue("Expected 1.0 km or 1,0 km but was: $result", 
            result == "1.0 km" || result == "1,0 km")
    }

    @Test
    fun `formatDistance returns km with decimal for large distance`() {
        val result = DistanceCalculator.formatDistance(2500f)
        assertTrue("Expected 2.5 km or 2,5 km but was: $result", 
            result == "2.5 km" || result == "2,5 km")
    }

    @Test
    fun `formatDistance rounds to one decimal place`() {
        val result = DistanceCalculator.formatDistance(1550f)
        assertTrue("Expected 1.5 km or 1,5 km but was: $result", 
            result == "1.5 km" || result == "1,5 km" || result == "1.6 km" || result == "1,6 km")
    }

    @Test
    fun `formatDistance handles very large distance`() {
        val result = DistanceCalculator.formatDistance(10000f)
        assertTrue("Expected 10.0 km or 10,0 km but was: $result", 
            result == "10.0 km" || result == "10,0 km")
    }

    @Test
    fun `formatDistance truncates decimal in meters`() {
        // 567.8f should become "567 meters" (toInt truncates)
        assertEquals("567 meters", DistanceCalculator.formatDistance(567.8f))
    }
}
