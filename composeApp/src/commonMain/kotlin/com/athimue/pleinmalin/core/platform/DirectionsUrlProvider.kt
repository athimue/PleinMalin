package com.athimue.pleinmalin.core.platform

import com.athimue.pleinmalin.domain.model.GeoPoint

/**
 * Construit l'URL d'itinéraire vers une destination, adaptée à l'app de cartes de la plateforme
 * (Google Maps sur Android, Plans sur iOS). Implémentations dans androidMain / iosMain.
 */
fun interface DirectionsUrlProvider {
    fun directionsUrl(destination: GeoPoint): String
}
