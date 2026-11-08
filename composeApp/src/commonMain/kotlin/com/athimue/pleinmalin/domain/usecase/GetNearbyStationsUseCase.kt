package com.athimue.pleinmalin.domain.usecase

import com.athimue.pleinmalin.core.domain.DataError
import com.athimue.pleinmalin.core.domain.Outcome
import com.athimue.pleinmalin.core.domain.map
import com.athimue.pleinmalin.domain.location.LocationProvider
import com.athimue.pleinmalin.domain.model.FuelType
import com.athimue.pleinmalin.domain.model.NearbyStation
import com.athimue.pleinmalin.domain.model.SortOrder
import com.athimue.pleinmalin.domain.model.sortedFor
import com.athimue.pleinmalin.domain.repository.StationRepository

class GetNearbyStationsUseCase(
    private val repository: StationRepository,
    private val locationProvider: LocationProvider,
) {
    suspend operator fun invoke(
        fuel: FuelType,
        radiusKm: Int,
        sortOrder: SortOrder,
    ): Outcome<List<NearbyStation>, DataError> {
        val origin = locationProvider.currentLocation()
        return repository.getStationsAround(origin, radiusKm, fuel).map { stations ->
            stations
                .mapNotNull { station ->
                    station.priceOf(fuel)?.let { price ->
                        NearbyStation(
                            station = station,
                            price = price,
                            distanceKm = origin.distanceTo(station.location),
                        )
                    }
                }
                .sortedFor(sortOrder)
        }
    }
}
