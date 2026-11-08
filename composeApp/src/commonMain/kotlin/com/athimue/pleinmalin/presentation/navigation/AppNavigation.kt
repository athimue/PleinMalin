package com.athimue.pleinmalin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.athimue.pleinmalin.presentation.stationdetail.StationDetailRoot
import com.athimue.pleinmalin.presentation.stationlist.StationListRoot
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object StationList : Route

    @Serializable
    data class StationDetail(val stationId: String) : Route
}

/**
 * Sur iOS il n'y a pas de sérialisation par réflexion : on déclare explicitement
 * les sous-types de NavKey pour que la back stack survive à la recréation de l'écran.
 */
private val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.StationList::class, Route.StationList.serializer())
            subclass(Route.StationDetail::class, Route.StationDetail.serializer())
        }
    }
}

@Composable
fun AppNavigation() {
    // Avec Navigation 3, la back stack est une simple liste observable que l'on manipule soi-même.
    val backStack = rememberNavBackStack(navigationConfig, Route.StationList)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // Un ViewModelStore par entrée : chaque écran détail a son propre ViewModel.
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Route.StationList> {
                StationListRoot(
                    onNavigateToDetail = { stationId -> backStack.add(Route.StationDetail(stationId)) },
                )
            }
            entry<Route.StationDetail> { route ->
                StationDetailRoot(
                    stationId = route.stationId,
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
