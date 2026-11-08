package com.athimue.pleinmalin.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Réponse de l'API Explore v2.1 (Opendatasoft) de data.economie.gouv.fr.
 * Jeu de données : prix-des-carburants-en-france-flux-instantane-v2.
 */
@Serializable
data class RecordsResponseDto(
    @SerialName("total_count") val totalCount: Int = 0,
    val results: List<StationDto> = emptyList(),
)

/**
 * Tous les champs sont optionnels : la donnée est déclarée par les stations,
 * certaines ne renseignent pas tous les carburants.
 */
@Serializable
data class StationDto(
    val id: String? = null,
    val adresse: String? = null,
    val ville: String? = null,
    val cp: String? = null,
    /** "R" = route, "A" = autoroute. */
    val pop: String? = null,
    val geom: GeomDto? = null,
    @SerialName("horaires_automate_24_24") val automate24h: String? = null,
    /** Liste de services ; gardé en JsonElement car le format (tableau ou texte) peut varier. */
    @SerialName("services_service") val services: JsonElement? = null,

    @SerialName("gazole_prix") val gazolePrix: Double? = null,
    @SerialName("gazole_maj") val gazoleMaj: String? = null,
    @SerialName("sp95_prix") val sp95Prix: Double? = null,
    @SerialName("sp95_maj") val sp95Maj: String? = null,
    @SerialName("sp98_prix") val sp98Prix: Double? = null,
    @SerialName("sp98_maj") val sp98Maj: String? = null,
    @SerialName("e10_prix") val e10Prix: Double? = null,
    @SerialName("e10_maj") val e10Maj: String? = null,
    @SerialName("e85_prix") val e85Prix: Double? = null,
    @SerialName("e85_maj") val e85Maj: String? = null,
    @SerialName("gplc_prix") val gplcPrix: Double? = null,
    @SerialName("gplc_maj") val gplcMaj: String? = null,
)

@Serializable
data class GeomDto(
    val lon: Double,
    val lat: Double,
)
