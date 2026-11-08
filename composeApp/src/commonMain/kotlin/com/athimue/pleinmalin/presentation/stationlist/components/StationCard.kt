package com.athimue.pleinmalin.presentation.stationlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.athimue.pleinmalin.domain.model.FuelPrice
import com.athimue.pleinmalin.domain.model.FuelType
import com.athimue.pleinmalin.domain.model.GeoPoint
import com.athimue.pleinmalin.domain.model.NearbyStation
import com.athimue.pleinmalin.domain.model.Station
import com.athimue.pleinmalin.presentation.common.formatDistance
import com.athimue.pleinmalin.presentation.common.formatPrice
import com.athimue.pleinmalin.presentation.common.updatedAgoLabel
import com.athimue.pleinmalin.presentation.theme.PleinMalinTheme
import com.athimue.pleinmalin.resources.Res
import com.athimue.pleinmalin.resources.badge_24h
import com.athimue.pleinmalin.resources.badge_cheapest
import com.athimue.pleinmalin.resources.badge_highway
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@Composable
fun StationCard(
    nearby: NearbyStation,
    isCheapest: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val station = nearby.station
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = if (isCheapest) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = station.address,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${station.postalCode} ${station.city} · ${formatDistance(nearby.distanceKm)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                updatedAgoLabel(nearby.price.updatedAt)?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isCheapest) Tag(stringResource(Res.string.badge_cheapest))
                    if (station.isOnHighway) Tag(stringResource(Res.string.badge_highway))
                    if (station.isOpen24h) Tag(stringResource(Res.string.badge_24h))
                }
            }
            Text(
                text = formatPrice(nearby.price.price),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun Tag(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Preview
@Composable
private fun StationCardPreview() {
    PleinMalinTheme {
        StationCard(
            nearby = NearbyStation(
                station = Station(
                    id = "75012001",
                    address = "12 Avenue Daumesnil",
                    postalCode = "75012",
                    city = "Paris",
                    location = GeoPoint(48.84, 2.38),
                    isOnHighway = false,
                    isOpen24h = true,
                    prices = emptyList(),
                    services = emptyList(),
                ),
                price = FuelPrice(FuelType.GAZOLE, 1.749, Clock.System.now() - 3.hours),
                distanceKm = 2.4,
            ),
            isCheapest = true,
            onClick = {},
        )
    }
}
