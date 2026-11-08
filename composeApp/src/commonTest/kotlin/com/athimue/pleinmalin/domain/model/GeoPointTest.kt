package com.athimue.pleinmalin.domain.model

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeoPointTest {

    @Test
    fun distance_paris_lyon_is_about_392_km() {
        val paris = GeoPoint(48.8566, 2.3522)
        val lyon = GeoPoint(45.7640, 4.8357)

        val distance = paris.distanceTo(lyon)

        assertTrue(abs(distance - 392.0) < 5.0, "distance attendue ≈ 392 km, obtenue $distance")
    }

    @Test
    fun distance_to_itself_is_zero() {
        val point = GeoPoint(43.3193, -0.3604)
        assertEquals(0.0, point.distanceTo(point), absoluteTolerance = 1e-9)
    }
}
