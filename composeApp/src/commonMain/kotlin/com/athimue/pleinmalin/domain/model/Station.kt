package com.athimue.pleinmalin.domain.model

import kotlin.time.Instant

enum class FuelType {
    GAZOLE,
    SP95,
    SP98,
    E10,
    E85,
    GPLC,
}

data class FuelPrice(
    val fuel: FuelType,
    /** Prix au litre, en euros. */
    val price: Double,
    val updatedAt: Instant?,
)

data class Station(
    val id: String,
    val address: String,
    val postalCode: String,
    val city: String,
    val location: GeoPoint,
    val isOnHighway: Boolean,
    val isOpen24h: Boolean,
    val prices: List<FuelPrice>,
    val services: List<String>,
) {
    fun priceOf(fuel: FuelType): FuelPrice? = prices.firstOrNull { it.fuel == fuel }
}

/** Une station vue depuis la position de l'utilisateur, pour un carburant donné. */
data class NearbyStation(
    val station: Station,
    val price: FuelPrice,
    val distanceKm: Double,
)

enum class SortOrder {
    PRICE,
    DISTANCE,
}

fun List<NearbyStation>.sortedFor(order: SortOrder): List<NearbyStation> = when (order) {
    SortOrder.PRICE -> sortedWith(compareBy({ it.price.price }, { it.distanceKm }))
    SortOrder.DISTANCE -> sortedBy { it.distanceKm }
}
