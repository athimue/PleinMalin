package com.athimue.pleinmalin.presentation.stationdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athimue.pleinmalin.core.domain.onFailure
import com.athimue.pleinmalin.core.domain.onSuccess
import com.athimue.pleinmalin.core.platform.DirectionsUrlProvider
import com.athimue.pleinmalin.domain.usecase.GetStationUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StationDetailViewModel(
    private val stationId: String,
    private val getStation: GetStationUseCase,
    private val directionsUrlProvider: DirectionsUrlProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(StationDetailState())
    private var hasLoadedInitialData = false

    val state: StateFlow<StationDetailState> = _state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                loadStation()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = _state.value,
        )

    private val _effects = Channel<StationDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: StationDetailIntent) {
        when (intent) {
            StationDetailIntent.DirectionsClicked -> {
                val station = _state.value.station ?: return
                val url = directionsUrlProvider.directionsUrl(station.location)
                viewModelScope.launch { _effects.send(StationDetailEffect.OpenUrl(url)) }
            }

            StationDetailIntent.BackClicked -> viewModelScope.launch {
                _effects.send(StationDetailEffect.NavigateBack)
            }

            StationDetailIntent.Retry -> loadStation()
        }
    }

    private fun loadStation() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getStation(stationId)
                .onSuccess { station -> _state.update { it.copy(isLoading = false, station = station) } }
                .onFailure { error -> _state.update { it.copy(isLoading = false, error = error) } }
        }
    }
}
