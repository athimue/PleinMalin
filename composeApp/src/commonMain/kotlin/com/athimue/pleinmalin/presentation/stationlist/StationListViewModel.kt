package com.athimue.pleinmalin.presentation.stationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athimue.pleinmalin.core.domain.onFailure
import com.athimue.pleinmalin.core.domain.onSuccess
import com.athimue.pleinmalin.domain.model.sortedFor
import com.athimue.pleinmalin.domain.usecase.GetNearbyStationsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StationListViewModel(
    private val getNearbyStations: GetNearbyStationsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StationListState())
    private var hasLoadedInitialData = false

    /** Chargement déclenché au premier abonnement de l'UI, pas dans init {} (plus simple à tester). */
    val state: StateFlow<StationListState> = _state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                loadStations()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = _state.value,
        )

    private val _effects = Channel<StationListEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var loadJob: Job? = null

    fun onIntent(intent: StationListIntent) {
        when (intent) {
            is StationListIntent.FuelSelected -> {
                if (intent.fuel == _state.value.selectedFuel) return
                _state.update { it.copy(selectedFuel = intent.fuel, stations = emptyList()) }
                loadStations()
            }

            is StationListIntent.SortOrderSelected -> _state.update {
                // Pas besoin de rappeler l'API : on retrie la liste en mémoire.
                it.copy(sortOrder = intent.sortOrder, stations = it.stations.sortedFor(intent.sortOrder))
            }

            is StationListIntent.StationClicked -> viewModelScope.launch {
                _effects.send(StationListEffect.NavigateToDetail(intent.stationId))
            }

            StationListIntent.Refresh -> loadStations(isRefresh = true)
            StationListIntent.Retry -> loadStations()
        }
    }

    private fun loadStations(isRefresh: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, error = null) }
            val current = _state.value
            getNearbyStations(
                fuel = current.selectedFuel,
                radiusKm = current.radiusKm,
                sortOrder = current.sortOrder,
            ).onSuccess { stations ->
                _state.update { it.copy(isLoading = false, isRefreshing = false, stations = stations) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, isRefreshing = false, error = error) }
            }
        }
    }
}
