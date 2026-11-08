package com.athimue.pleinmalin.presentation.stationlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.athimue.pleinmalin.domain.model.FuelType
import com.athimue.pleinmalin.domain.model.SortOrder
import com.athimue.pleinmalin.presentation.common.ErrorView
import com.athimue.pleinmalin.presentation.common.LoadingView
import com.athimue.pleinmalin.presentation.common.MessageView
import com.athimue.pleinmalin.presentation.common.ObserveAsEvents
import com.athimue.pleinmalin.presentation.common.label
import com.athimue.pleinmalin.presentation.stationlist.components.StationCard
import com.athimue.pleinmalin.resources.Res
import com.athimue.pleinmalin.resources.app_name
import com.athimue.pleinmalin.resources.empty_stations
import com.athimue.pleinmalin.resources.sort_by_distance
import com.athimue.pleinmalin.resources.sort_by_price
import com.athimue.pleinmalin.resources.station_list_subtitle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Point d'entrée "stateful" : branche le ViewModel et les effets sur l'écran "stateless". */
@Composable
fun StationListRoot(
    onNavigateToDetail: (stationId: String) -> Unit,
    viewModel: StationListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is StationListEffect.NavigateToDetail -> onNavigateToDetail(effect.stationId)
        }
    }

    StationListScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationListScreen(
    state: StationListState,
    onIntent: (StationListIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(Res.string.app_name))
                        Text(
                            text = stringResource(Res.string.station_list_subtitle, state.radiusKm),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            FuelSelector(
                selected = state.selectedFuel,
                onSelect = { onIntent(StationListIntent.FuelSelected(it)) },
            )
            SortSelector(
                selected = state.sortOrder,
                onSelect = { onIntent(StationListIntent.SortOrderSelected(it)) },
            )

            when {
                state.isLoading -> LoadingView()
                state.error != null && state.stations.isEmpty() -> ErrorView(
                    error = state.error,
                    onRetry = { onIntent(StationListIntent.Retry) },
                )
                else -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onIntent(StationListIntent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (state.stations.isEmpty()) {
                        MessageView(text = stringResource(Res.string.empty_stations, state.radiusKm))
                    } else {
                        val cheapest = state.cheapestPrice
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(state.stations, key = { it.station.id }) { nearby ->
                                StationCard(
                                    nearby = nearby,
                                    isCheapest = nearby.price.price == cheapest,
                                    onClick = { onIntent(StationListIntent.StationClicked(nearby.station.id)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FuelSelector(
    selected: FuelType,
    onSelect: (FuelType) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(FuelType.entries) { fuel ->
            FilterChip(
                selected = fuel == selected,
                onClick = { onSelect(fuel) },
                label = { Text(fuel.label) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSelector(
    selected: SortOrder,
    onSelect: (SortOrder) -> Unit,
) {
    val options = listOf(
        SortOrder.PRICE to stringResource(Res.string.sort_by_price),
        SortOrder.DISTANCE to stringResource(Res.string.sort_by_distance),
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        options.forEachIndexed { index, (order, label) ->
            SegmentedButton(
                selected = order == selected,
                onClick = { onSelect(order) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) {
                Text(label)
            }
        }
    }
}
