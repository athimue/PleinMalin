package com.athimue.pleinmalin.data.remote

import com.athimue.pleinmalin.data.remote.dto.RecordsResponseDto
import com.athimue.pleinmalin.data.remote.dto.StationDto
import com.athimue.pleinmalin.domain.model.FuelType
import com.athimue.pleinmalin.domain.model.GeoPoint
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Accès à l'API. Les filtres sont écrits en ODSQL, le langage de requête d'Opendatasoft :
 * https://help.opendatasoft.com/apis/ods-explore-v2/
 */
class StationRemoteDataSource(
    private val client: HttpClient,
) {
    suspend fun fetchStationsAround(
        center: GeoPoint,
        radiusKm: Int,
        fuel: FuelType,
        limit: Int = MAX_LIMIT,
    ): RecordsResponseDto {
        val priceField = "${fuel.apiField}_prix"
        return client.get(RECORDS_PATH) {
            parameter(
                "where",
                "within_distance(geom, geom'POINT(${center.longitude} ${center.latitude})', ${radiusKm}km) " +
                    "AND $priceField IS NOT NULL",
            )
            parameter("order_by", "$priceField ASC")
            parameter("limit", limit)
        }.body()
    }

    suspend fun fetchStation(id: String): StationDto? {
        // L'identifiant est numérique côté API : on refuse tout autre format pour ne pas injecter d'ODSQL.
        require(id.all(Char::isDigit)) { "Identifiant de station invalide : $id" }
        return client.get(RECORDS_PATH) {
            parameter("where", "id=$id")
            parameter("limit", 1)
        }.body<RecordsResponseDto>().results.firstOrNull()
    }

    private companion object {
        const val RECORDS_PATH = "catalog/datasets/prix-des-carburants-en-france-flux-instantane-v2/records"

        /** Limite maximale autorisée par l'endpoint /records. */
        const val MAX_LIMIT = 100
    }
}

/** Préfixe des colonnes de l'API pour chaque carburant (gazole_prix, sp95_maj, …). */
internal val FuelType.apiField: String
    get() = when (this) {
        FuelType.GAZOLE -> "gazole"
        FuelType.SP95 -> "sp95"
        FuelType.SP98 -> "sp98"
        FuelType.E10 -> "e10"
        FuelType.E85 -> "e85"
        FuelType.GPLC -> "gplc"
    }
