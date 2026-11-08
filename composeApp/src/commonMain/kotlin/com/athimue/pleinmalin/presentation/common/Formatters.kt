package com.athimue.pleinmalin.presentation.common

import androidx.compose.runtime.Composable
import com.athimue.pleinmalin.core.domain.DataError
import com.athimue.pleinmalin.domain.model.FuelType
import com.athimue.pleinmalin.resources.Res
import com.athimue.pleinmalin.resources.error_no_internet
import com.athimue.pleinmalin.resources.error_not_found
import com.athimue.pleinmalin.resources.error_serialization
import com.athimue.pleinmalin.resources.error_server
import com.athimue.pleinmalin.resources.error_timeout
import com.athimue.pleinmalin.resources.error_unknown
import com.athimue.pleinmalin.resources.updated_days_ago
import com.athimue.pleinmalin.resources.updated_hours_ago
import com.athimue.pleinmalin.resources.updated_minutes_ago
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

// Pas de String.format en commonMain : on formate à la main, à la française.

/** 1.7489 -> "1,749 €" */
fun formatPrice(price: Double): String {
    val thousandths = (price * 1000).roundToLong()
    val euros = thousandths / 1000
    val decimals = (thousandths % 1000).toString().padStart(3, '0')
    return "$euros,$decimals €"
}

/** 0.85 -> "850 m", 3.24 -> "3,2 km" */
fun formatDistance(km: Double): String =
    if (km < 1.0) {
        "${(km * 1000).roundToInt()} m"
    } else {
        val tenths = (km * 10).roundToInt()
        "${tenths / 10},${tenths % 10} km"
    }

val FuelType.label: String
    get() = when (this) {
        FuelType.GAZOLE -> "Gazole"
        FuelType.SP95 -> "SP95"
        FuelType.SP98 -> "SP98"
        FuelType.E10 -> "SP95-E10"
        FuelType.E85 -> "E85"
        FuelType.GPLC -> "GPLc"
    }

val DataError.message: StringResource
    get() = when (this) {
        DataError.NO_INTERNET -> Res.string.error_no_internet
        DataError.TIMEOUT -> Res.string.error_timeout
        DataError.SERVER -> Res.string.error_server
        DataError.NOT_FOUND -> Res.string.error_not_found
        DataError.SERIALIZATION -> Res.string.error_serialization
        DataError.UNKNOWN -> Res.string.error_unknown
    }

@Composable
fun updatedAgoLabel(updatedAt: Instant?, now: Instant = Clock.System.now()): String? {
    if (updatedAt == null) return null
    val elapsed = (now - updatedAt).coerceAtLeast(kotlin.time.Duration.ZERO)
    return when {
        elapsed < 1.hours -> stringResource(
            Res.string.updated_minutes_ago,
            elapsed.inWholeMinutes.coerceAtLeast(1).toInt(),
        )
        elapsed < 1.days -> stringResource(Res.string.updated_hours_ago, elapsed.inWholeHours.toInt())
        else -> stringResource(Res.string.updated_days_ago, elapsed.inWholeDays.toInt())
    }
}
