package com.athimue.pleinmalin.data.location

import com.athimue.pleinmalin.domain.location.LocationProvider
import com.athimue.pleinmalin.domain.model.GeoPoint

/** Position simulée (Paris, Hôtel de Ville) en attendant la vraie géolocalisation. */
class FixedLocationProvider(
    private val position: GeoPoint = GeoPoint(latitude = 48.8566, longitude = 2.3522),
) : LocationProvider {
    override suspend fun currentLocation(): GeoPoint = position
}
