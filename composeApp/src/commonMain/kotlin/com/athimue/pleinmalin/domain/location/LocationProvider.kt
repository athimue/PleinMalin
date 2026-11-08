package com.athimue.pleinmalin.domain.location

import com.athimue.pleinmalin.domain.model.GeoPoint

/**
 * Source de la position de l'utilisateur.
 *
 * Pour l'instant implémentée par une position fixe (FixedLocationProvider).
 * Étape suivante : une implémentation par plateforme (FusedLocationProvider / CLLocationManager)
 * fournie via le platformModule de Koin.
 */
interface LocationProvider {
    suspend fun currentLocation(): GeoPoint
}
