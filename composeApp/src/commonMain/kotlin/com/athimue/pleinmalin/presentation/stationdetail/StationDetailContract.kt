package com.athimue.pleinmalin.presentation.stationdetail

import com.athimue.pleinmalin.core.domain.DataError
import com.athimue.pleinmalin.domain.model.Station

data class StationDetailState(
    val isLoading: Boolean = true,
    val station: Station? = null,
    val error: DataError? = null,
)

sealed interface StationDetailIntent {
    data object DirectionsClicked : StationDetailIntent
    data object BackClicked : StationDetailIntent
    data object Retry : StationDetailIntent
}

sealed interface StationDetailEffect {
    data class OpenUrl(val url: String) : StationDetailEffect
    data object NavigateBack : StationDetailEffect
}
