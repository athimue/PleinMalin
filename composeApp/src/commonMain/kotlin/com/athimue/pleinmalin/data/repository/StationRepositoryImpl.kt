package com.athimue.pleinmalin.data.repository

import com.athimue.pleinmalin.core.domain.DataError
import com.athimue.pleinmalin.core.domain.Outcome
import com.athimue.pleinmalin.core.domain.onSuccess
import com.athimue.pleinmalin.data.mapper.toDomain
import com.athimue.pleinmalin.data.remote.StationRemoteDataSource
import com.athimue.pleinmalin.domain.model.FuelType
import com.athimue.pleinmalin.domain.model.GeoPoint
import com.athimue.pleinmalin.domain.model.Station
import com.athimue.pleinmalin.domain.repository.StationRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * Cache mémoire simple : l'écran détail réutilise la station déjà chargée par la liste.
 * À remplacer par Room 3 (source unique de vérité) à l'étape "favoris / hors-ligne".
 */
class StationRepositoryImpl(
    private val remote: StationRemoteDataSource,
) : StationRepository {

    private val cache = MutableStateFlow<Map<String, Station>>(emptyMap())

    override suspend fun getStationsAround(
        center: GeoPoint,
        radiusKm: Int,
        fuel: FuelType,
    ): Outcome<List<Station>, DataError> =
        safeCall {
            remote.fetchStationsAround(center, radiusKm, fuel).results.mapNotNull { it.toDomain() }
        }.onSuccess { stations ->
            cache.update { current -> current + stations.associateBy(Station::id) }
        }

    override suspend fun getStation(id: String): Outcome<Station, DataError> {
        cache.value[id]?.let { return Outcome.Success(it) }
        return when (val result = safeCall { remote.fetchStation(id)?.toDomain() }) {
            is Outcome.Failure -> result
            is Outcome.Success -> result.data
                ?.let { station ->
                    cache.update { it + (station.id to station) }
                    Outcome.Success(station)
                }
                ?: Outcome.Failure(DataError.NOT_FOUND)
        }
    }

    private inline fun <T> safeCall(block: () -> T): Outcome<T, DataError> = try {
        Outcome.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        Outcome.Failure(DataError.TIMEOUT)
    } catch (e: ClientRequestException) {
        Outcome.Failure(if (e.response.status.value == 404) DataError.NOT_FOUND else DataError.UNKNOWN)
    } catch (e: ServerResponseException) {
        Outcome.Failure(DataError.SERVER)
    } catch (e: ContentConvertException) {
        Outcome.Failure(DataError.SERIALIZATION)
    } catch (e: SerializationException) {
        Outcome.Failure(DataError.SERIALIZATION)
    } catch (e: IOException) {
        Outcome.Failure(DataError.NO_INTERNET)
    } catch (e: Exception) {
        Outcome.Failure(DataError.UNKNOWN)
    }
}
