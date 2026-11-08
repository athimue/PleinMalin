package com.athimue.pleinmalin.di

import com.athimue.pleinmalin.core.platform.DirectionsUrlProvider
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single {
        DirectionsUrlProvider { destination ->
            // Ouvre l'app Google Maps si elle est installée, sinon le navigateur.
            "https://www.google.com/maps/dir/?api=1&destination=${destination.latitude},${destination.longitude}"
        }
    }
}
