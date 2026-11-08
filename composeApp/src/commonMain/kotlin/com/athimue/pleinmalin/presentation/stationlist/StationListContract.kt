package com.athimue.pleinmalin.presentation.stationlist

import com.athimue.pleinmalin.core.domain.DataError
import com.athimue.pleinmalin.domain.model.FuelType
import com.athimue.pleinmalin.domain.model.NearbyStation
import com.athimue.pleinmalin.domain.model.SortOrder

data class StationListState(
    val selectedFuel: FuelType = FuelType.GAZOLE,
    val sortOrder: SortOrder = SortOrder.PRICE,
    val radiusKm: Int = 10,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val stations: List<NearbyStation> = emptyList(),
    val error: DataError? = null,
) {
    val cheapestPrice: Double? get() = stations.minOfOrNull { it.price.price }
}

sealed interface StationListIntent {
    data class FuelSelected(val fuel: FuelType) : StationListIntent
    data class SortOrderSelected(val sortOrder: SortOrder) : StationListIntent
    data class StationClicked(val stationId: String) : StationListIntent
    data object Refresh : StationListIntent
    data object Retry : StationListIntent
}

sealed interface StationListEffect {
    data class NavigateToDetail(val stationId: String) : StationListEffect
}
