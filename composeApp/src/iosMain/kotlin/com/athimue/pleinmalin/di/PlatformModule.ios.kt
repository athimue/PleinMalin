package com.athimue.pleinmalin.di

import com.athimue.pleinmalin.core.platform.DirectionsUrlProvider
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single {
        DirectionsUrlProvider { destination ->
            // Ouvre l'app Plans avec un itinéraire en voiture.
            "https://maps.apple.com/?daddr=${destination.latitude},${destination.longitude}&dirflg=d"
        }
    }
}
