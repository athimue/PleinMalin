package com.athimue.pleinmalin.di

import com.athimue.pleinmalin.core.network.createHttpClient
import com.athimue.pleinmalin.data.location.FixedLocationProvider
import com.athimue.pleinmalin.data.remote.StationRemoteDataSource
import com.athimue.pleinmalin.data.repository.StationRepositoryImpl
import com.athimue.pleinmalin.domain.location.LocationProvider
import com.athimue.pleinmalin.domain.repository.StationRepository
import com.athimue.pleinmalin.domain.usecase.GetNearbyStationsUseCase
import com.athimue.pleinmalin.domain.usecase.GetStationUseCase
import com.athimue.pleinmalin.presentation.stationdetail.StationDetailViewModel
import com.athimue.pleinmalin.presentation.stationlist.StationListViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/** Fourni par androidMain / iosMain : engine Ktor, services propres à chaque OS. */
expect val platformModule: Module

val networkModule = module {
    single { createHttpClient(engine = get()) }
}

val dataModule = module {
    singleOf(::StationRemoteDataSource)
    single<StationRepository> { StationRepositoryImpl(remote = get()) }
    single<LocationProvider> { FixedLocationProvider() }
}

val domainModule = module {
    factoryOf(::GetNearbyStationsUseCase)
    factoryOf(::GetStationUseCase)
}

val presentationModule = module {
    viewModelOf(::StationListViewModel)
    viewModel { (stationId: String) ->
        StationDetailViewModel(
            stationId = stationId,
            getStation = get(),
            directionsUrlProvider = get(),
        )
    }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(platformModule, networkModule, dataModule, domainModule, presentationModule)
    }
}
