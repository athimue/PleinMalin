package com.athimue.pleinmalin.domain.usecase

import com.athimue.pleinmalin.core.domain.DataError
import com.athimue.pleinmalin.core.domain.Outcome
import com.athimue.pleinmalin.domain.location.LocationProvider
import com.athimue.pleinmalin.domain.model.FuelPrice
import com.athimue.pleinmalin.domain.model.FuelType
import com.athimue.pleinmalin.domain.model.GeoPoint
import com.athimue.pleinmalin.domain.model.NearbyStation
import com.athimue.pleinmalin.domain.model.SortOrder
import com.athimue.pleinmalin.domain.model.Station
import com.athimue.pleinmalin.domain.repository.StationRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetNearbyStationsUseCaseTest {

    private val origin = GeoPoint(48.8566, 2.3522)

    // ~1 km, ~5 km et ~3 km de l'origine
    private val near = station("near", lat = 48.8656, gazole = 1.80)
    private val far = station("far", lat = 48.9016, gazole = 1.70)
    private val middle = station("middle", lat = 48.8836, gazole = 1.70)
    private val noGazole = station("no-gazole", lat = 48.8600, gazole = null)

    private val repository = FakeStationRepository(listOf(near, far, middle, noGazole))
    private val useCase = GetNearbyStationsUseCase(repository, FakeLocationProvider(origin))

    @Test
    fun sorts_by_price_then_distance() = runTest {
        val result = useCase(FuelType.GAZOLE, radiusKm = 10, sortOrder = SortOrder.PRICE)

        val ids = assertIs<Outcome.Success<List<NearbyStation>>>(result)
            .data.map { it.station.id }
        assertEquals(listOf("middle", "far", "near"), ids)
    }

    @Test
    fun sorts_by_distance() = runTest {
        val result = useCase(FuelType.GAZOLE, radiusKm = 10, sortOrder = SortOrder.DISTANCE)

        val ids = assertIs<Outcome.Success<List<NearbyStation>>>(result)
            .data.map { it.station.id }
        assertEquals(listOf("near", "middle", "far"), ids)
    }

    @Test
    fun excludes_stations_without_the_selected_fuel() = runTest {
        val result = useCase(FuelType.GAZOLE, radiusKm = 10, sortOrder = SortOrder.PRICE)

        val ids = assertIs<Outcome.Success<List<NearbyStation>>>(result)
            .data.map { it.station.id }
        assertEquals(false, "no-gazole" in ids)
    }

    @Test
    fun propagates_repository_errors() = runTest {
        repository.error = DataError.NO_INTERNET

        val result = useCase(FuelType.GAZOLE, radiusKm = 10, sortOrder = SortOrder.PRICE)

        assertEquals(Outcome.Failure(DataError.NO_INTERNET), result)
    }

    private fun station(id: String, lat: Double, gazole: Double?) = Station(
        id = id,
        address = "",
        postalCode = "",
        city = "",
        location = GeoPoint(lat, origin.longitude),
        isOnHighway = false,
        isOpen24h = false,
        prices = listOfNotNull(gazole?.let { FuelPrice(FuelType.GAZOLE, it, updatedAt = null) }),
        services = emptyList(),
    )
}

private class FakeLocationProvider(private val position: GeoPoint) : LocationProvider {
    override suspend fun currentLocation(): GeoPoint = position
}

private class FakeStationRepository(private val stations: List<Station>) : StationRepository {
    var error: DataError? = null

    override suspend fun getStationsAround(
        center: GeoPoint,
        radiusKm: Int,
        fuel: FuelType,
    ): Outcome<List<Station>, DataError> = error?.let { Outcome.Failure(it) } ?: Outcome.Success(stations)

    override suspend fun getStation(id: String): Outcome<Station, DataError> =
        stations.firstOrNull { it.id == id }?.let { Outcome.Success(it) } ?: Outcome.Failure(DataError.NOT_FOUND)
}
