package com.athimue.pleinmalin.domain.model

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
) {
    /** Distance à vol d'oiseau (formule de haversine), en kilomètres. */
    fun distanceTo(other: GeoPoint): Double {
        val dLat = (other.latitude - latitude).toRadians()
        val dLon = (other.longitude - longitude).toRadians()
        val a = sin(dLat / 2).pow(2) +
            cos(latitude.toRadians()) * cos(other.latitude.toRadians()) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(a))
    }

    private fun Double.toRadians(): Double = this * kotlin.math.PI / 180.0

    private companion object {
        const val EARTH_RADIUS_KM = 6371.0
    }
}
