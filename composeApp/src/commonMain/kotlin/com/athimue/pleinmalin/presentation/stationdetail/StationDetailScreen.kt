package com.athimue.pleinmalin.presentation.stationdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.athimue.pleinmalin.domain.model.Station
import com.athimue.pleinmalin.presentation.common.ErrorView
import com.athimue.pleinmalin.presentation.common.LoadingView
import com.athimue.pleinmalin.presentation.common.ObserveAsEvents
import com.athimue.pleinmalin.presentation.common.formatPrice
import com.athimue.pleinmalin.presentation.common.label
import com.athimue.pleinmalin.presentation.common.updatedAgoLabel
import com.athimue.pleinmalin.resources.Res
import com.athimue.pleinmalin.resources.back
import com.athimue.pleinmalin.resources.detail_directions
import com.athimue.pleinmalin.resources.detail_no_services
import com.athimue.pleinmalin.resources.detail_prices_title
import com.athimue.pleinmalin.resources.detail_services_title
import com.athimue.pleinmalin.resources.ic_arrow_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun StationDetailRoot(
    stationId: String,
    onNavigateBack: () -> Unit,
    viewModel: StationDetailViewModel = koinViewModel { parametersOf(stationId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is StationDetailEffect.OpenUrl -> uriHandler.openUri(effect.url)
            StationDetailEffect.NavigateBack -> onNavigateBack()
        }
    }

    StationDetailScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailScreen(
    state: StationDetailState,
    onIntent: (StationDetailIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.station?.city.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = { onIntent(StationDetailIntent.BackClicked) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.fillMaxSize().padding(innerPadding)
        when {
            state.isLoading -> LoadingView(contentModifier)
            state.station != null -> StationDetailContent(
                station = state.station,
                onDirectionsClick = { onIntent(StationDetailIntent.DirectionsClicked) },
                modifier = contentModifier,
            )
            state.error != null -> ErrorView(
                error = state.error,
                onRetry = { onIntent(StationDetailIntent.Retry) },
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun StationDetailContent(
    station: Station,
    onDirectionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(station.address, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "${station.postalCode} ${station.city}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Button(onClick = onDirectionsClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.detail_directions))
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.detail_prices_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                station.prices.forEachIndexed { index, fuelPrice ->
                    if (index > 0) HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(fuelPrice.fuel.label, style = MaterialTheme.typography.bodyLarge)
                            updatedAgoLabel(fuelPrice.updatedAt)?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            text = formatPrice(fuelPrice.price),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(Res.string.detail_services_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (station.services.isEmpty()) {
                    Text(stringResource(Res.string.detail_no_services), style = MaterialTheme.typography.bodyMedium)
                } else {
                    station.services.forEach { service ->
                        Text("• $service", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
