package com.athimue.pleinmalin.data.repository

import com.athimue.pleinmalin.core.domain.DataError
import com.athimue.pleinmalin.core.domain.Outcome
import com.athimue.pleinmalin.core.network.createHttpClient
import com.athimue.pleinmalin.data.remote.StationRemoteDataSource
import com.athimue.pleinmalin.domain.model.FuelType
import com.athimue.pleinmalin.domain.model.GeoPoint
import com.athimue.pleinmalin.domain.model.Station
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StationRepositoryImplTest {

    private val paris = GeoPoint(48.8566, 2.3522)

    @Test
    fun maps_api_records_to_domain_stations() = runTest {
        val repository = repositoryRespondingWith { respondJson(SAMPLE_RESPONSE) }

        val result = repository.getStationsAround(paris, radiusKm = 5, fuel = FuelType.GAZOLE)

        val stations = assertIs<Outcome.Success<List<Station>>>(result).data
        assertEquals(1, stations.size, "la station sans coordonnées doit être ignorée")
        val station = stations.single()
        assertEquals("75012001", station.id)
        assertEquals("12 Avenue Daumesnil", station.address)
        assertEquals("Paris", station.city)
        assertEquals(GeoPoint(48.845, 2.387), station.location)
        assertTrue(station.isOpen24h)
        assertEquals(listOf("Boutique alimentaire", "Lavage automatique"), station.services)

        val gazole = assertNotNull(station.priceOf(FuelType.GAZOLE))
        assertEquals(1.749, gazole.price)
        assertNotNull(gazole.updatedAt)
        assertEquals(null, station.priceOf(FuelType.E85))
    }

    @Test
    fun sends_a_geographic_odsql_filter() = runTest {
        var whereClause: String? = null
        val repository = repositoryRespondingWith { request ->
            whereClause = request.url.parameters["where"]
            respondJson("""{"total_count":0,"results":[]}""")
        }

        repository.getStationsAround(paris, radiusKm = 5, fuel = FuelType.SP98)

        val where = assertNotNull(whereClause)
        assertTrue(where.startsWith("within_distance(geom, geom'POINT(2.3522 48.8566)', 5km)"), where)
        assertTrue(where.endsWith("sp98_prix IS NOT NULL"), where)
    }

    @Test
    fun maps_server_errors() = runTest {
        val repository = repositoryRespondingWith { respond("oops", HttpStatusCode.ServiceUnavailable) }

        val result = repository.getStationsAround(paris, radiusKm = 5, fuel = FuelType.GAZOLE)

        assertEquals(Outcome.Failure(DataError.SERVER), result)
    }

    @Test
    fun detail_is_served_from_cache_after_a_list_call() = runTest {
        var calls = 0
        val repository = repositoryRespondingWith {
            calls++
            respondJson(SAMPLE_RESPONSE)
        }

        repository.getStationsAround(paris, radiusKm = 5, fuel = FuelType.GAZOLE)
        val detail = repository.getStation("75012001")

        assertIs<Outcome.Success<Station>>(detail)
        assertEquals(1, calls)
    }

    private fun repositoryRespondingWith(handler: MockRequestHandler) =
        StationRepositoryImpl(StationRemoteDataSource(createHttpClient(MockEngine(handler))))

    private fun io.ktor.client.engine.mock.MockRequestHandleScope.respondJson(body: String) = respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    private companion object {
        // Format de l'API Explore v2.1. Si l'API évolue, mettre à jour cet échantillon en premier.
        val SAMPLE_RESPONSE = """
            {
              "total_count": 2,
              "results": [
                {
                  "id": 75012001,
                  "cp": "75012",
                  "pop": "R",
                  "adresse": "12 AVENUE DAUMESNIL",
                  "ville": "PARIS",
                  "geom": { "lon": 2.387, "lat": 48.845 },
                  "horaires_automate_24_24": "Oui",
                  "services_service": ["Boutique alimentaire", "Lavage automatique"],
                  "gazole_prix": 1.749,
                  "gazole_maj": "2026-11-08T08:15:00+00:00",
                  "sp98_prix": 1.899,
                  "sp98_maj": "2026-11-07T17:40:00+00:00",
                  "e85_prix": null
                },
                {
                  "id": 75012002,
                  "adresse": "STATION SANS COORDONNEES",
                  "gazole_prix": 1.699
                }
              ]
            }
        """.trimIndent()
    }
}
