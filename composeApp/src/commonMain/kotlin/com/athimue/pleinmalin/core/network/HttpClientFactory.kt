package com.athimue.pleinmalin.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val PRIX_CARBURANTS_BASE_URL = "https://data.economie.gouv.fr/api/explore/v2.1/"

val appJson = Json {
    ignoreUnknownKeys = true
    // L'API mélange parfois nombres et chaînes (ex. id) : on reste tolérant.
    isLenient = true
    coerceInputValues = true
}

/** L'engine (OkHttp / Darwin) est fourni par le platformModule : c'est aussi ce qui rend le client testable. */
fun createHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(appJson)
    }
    install(HttpTimeout) {
        connectTimeoutMillis = 10_000
        requestTimeoutMillis = 20_000
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO
    }
    defaultRequest {
        url(PRIX_CARBURANTS_BASE_URL)
    }
}
