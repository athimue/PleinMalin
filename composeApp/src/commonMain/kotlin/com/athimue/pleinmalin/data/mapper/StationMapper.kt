package com.athimue.pleinmalin.data.mapper

import com.athimue.pleinmalin.data.remote.dto.StationDto
import com.athimue.pleinmalin.domain.model.FuelPrice
import com.athimue.pleinmalin.domain.model.FuelType
import com.athimue.pleinmalin.domain.model.GeoPoint
import com.athimue.pleinmalin.domain.model.Station
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Instant

/** Retourne null si la station est inexploitable (sans identifiant ni coordonnées). */
fun StationDto.toDomain(): Station? {
    val id = id ?: return null
    val geom = geom ?: return null
    return Station(
        id = id,
        address = adresse.orEmpty().toTitleCase(),
        postalCode = cp.orEmpty(),
        city = ville.orEmpty().toTitleCase(),
        location = GeoPoint(latitude = geom.lat, longitude = geom.lon),
        isOnHighway = pop == "A",
        isOpen24h = automate24h.equals("Oui", ignoreCase = true),
        prices = FuelType.entries.mapNotNull { fuel -> priceFor(fuel) },
        services = services.toStringList(),
    )
}

private fun StationDto.priceFor(fuel: FuelType): FuelPrice? {
    val (price, updatedAt) = when (fuel) {
        FuelType.GAZOLE -> gazolePrix to gazoleMaj
        FuelType.SP95 -> sp95Prix to sp95Maj
        FuelType.SP98 -> sp98Prix to sp98Maj
        FuelType.E10 -> e10Prix to e10Maj
        FuelType.E85 -> e85Prix to e85Maj
        FuelType.GPLC -> gplcPrix to gplcMaj
    }
    return price?.let { FuelPrice(fuel = fuel, price = it, updatedAt = updatedAt?.toInstantOrNull()) }
}

private fun String.toInstantOrNull(): Instant? = runCatching { Instant.parse(this) }.getOrNull()

private fun JsonElement?.toStringList(): List<String> = when (this) {
    null, JsonNull -> emptyList()
    is JsonArray -> mapNotNull { (it as? JsonPrimitive)?.content }
    is JsonPrimitive -> content.split("//").map(String::trim).filter(String::isNotEmpty)
    else -> emptyList()
}

/** Les adresses arrivent souvent en majuscules ("12 AVENUE DE LA REPUBLIQUE"). */
private fun String.toTitleCase(): String =
    lowercase().split(" ").joinToString(" ") { word -> word.replaceFirstChar(Char::uppercaseChar) }
