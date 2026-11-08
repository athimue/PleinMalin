package com.athimue.pleinmalin.domain.usecase

import com.athimue.pleinmalin.core.domain.DataError
import com.athimue.pleinmalin.core.domain.Outcome
import com.athimue.pleinmalin.domain.model.Station
import com.athimue.pleinmalin.domain.repository.StationRepository

class GetStationUseCase(
    private val repository: StationRepository,
) {
    suspend operator fun invoke(id: String): Outcome<Station, DataError> = repository.getStation(id)
}
