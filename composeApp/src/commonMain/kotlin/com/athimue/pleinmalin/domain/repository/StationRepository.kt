package com.athimue.pleinmalin.domain.repository

import com.athimue.pleinmalin.core.domain.DataError
import com.athimue.pleinmalin.core.domain.Outcome
import com.athimue.pleinmalin.domain.model.FuelType
import com.athimue.pleinmalin.domain.model.GeoPoint
import com.athimue.pleinmalin.domain.model.Station

interface StationRepository {
    /** Stations situées dans un rayon autour de [center] et qui vendent [fuel]. */
    suspend fun getStationsAround(
        center: GeoPoint,
        radiusKm: Int,
        fuel: FuelType,
    ): Outcome<List<Station>, DataError>

    suspend fun getStation(id: String): Outcome<Station, DataError>
}
