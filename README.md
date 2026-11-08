# Plein Malin

Trouver la station la moins chère autour de soi, sur Android et iOS, avec un seul code Kotlin.

Données : [Prix des carburants en France – flux instantané v2](https://data.economie.gouv.fr/explore/dataset/prix-des-carburants-en-france-flux-instantane-v2/)
(API Explore v2.1 d'Opendatasoft, sans clé, mise à jour toutes les 10 minutes environ).

## Structure

```
androidApp/   Point d'entrée Android (Application, MainActivity) — plugin com.android.application
composeApp/   Code partagé KMP — plugin com.android.kotlin.multiplatform.library (AGP 9)
  commonMain/kotlin/com/athimue/pleinmalin/
    core/          Outcome, DataError, client HTTP, interfaces plateforme
    domain/        Modèles, use cases, interfaces de repository (Kotlin pur, sans framework)
    data/          DTO, mapper, StationRemoteDataSource (Ktor), StationRepositoryImpl
    presentation/  MVI : Contract (State / Intent / Effect), ViewModel, Screen, navigation
    di/            Modules Koin (+ platformModule en expect/actual)
  androidMain/     Engine OkHttp, URL d'itinéraire Google Maps
  iosMain/         Engine Darwin, URL d'itinéraire Plans, MainViewController
iosApp/       Projet Xcode (SwiftUI qui héberge l'UI Compose)
```

Chaque écran suit le même schéma MVI :
`XxxContract.kt` (état immuable, intentions, effets one-shot) → `XxxViewModel` (`onIntent`, `StateFlow`, `Channel` d'effets)
→ `XxxRoot` (branche ViewModel et effets) → `XxxScreen` (stateless, prévisualisable).

## Stack

| Rôle | Choix |
|---|---|
| Langage / build | Kotlin 2.4.20, AGP 9.4.0, Gradle 9.6.0 |
| UI | Compose Multiplatform 1.12.2, Material 3, ressources Compose |
| Navigation | Navigation 3 (back stack sérialisable, un ViewModelStore par entrée) |
| Architecture | ViewModel Jetpack multiplateforme 2.11, MVI, Clean Architecture |
| Réseau | Ktor 3.6 (OkHttp / Darwin), kotlinx.serialization |
| DI | Koin 4.2 |
| Tests | kotlin-test, coroutines-test, Ktor MockEngine |

## Lancer le projet

- **Android** : ouvrir le dossier dans Android Studio, synchroniser Gradle, lancer la configuration `androidApp`.
- **iOS** (sur Mac) : ouvrir `iosApp/iosApp.xcodeproj` dans Xcode, renseigner `TEAM_ID` dans `iosApp/Configuration/Config.xcconfig` pour un appareil réel, puis Run.
- **Tests** : `./gradlew :composeApp:allTests`.

## Feuille de route

1. ~~Liste des stations proches triées par prix ou distance, écran détail, itinéraire~~ ✅
2. **Géolocalisation réelle** : implémenter `LocationProvider` par plateforme
   (FusedLocationProviderClient sur Android, CLLocationManager sur iOS) et l'enregistrer dans `platformModule`,
   avec la gestion des permissions. Ne pas oublier `NSLocationWhenInUseUsageDescription` dans `Info.plist`.
3. **Favoris et hors-ligne** avec Room 3 (`androidx.room3`, KSP, `BundledSQLiteDriver`) :
   la base devient la source unique de vérité, le réseau ne fait que la rafraîchir.
4. **Carte** des stations avec MapLibre Compose, puis variante `expect/actual` (Google Maps / MapKit) pour comparer.
5. **Modularisation** : `build-logic` avec convention plugins, modules `core:*` et `feature:*`.
6. **Interop Swift** : un écran en SwiftUI natif consommant les ViewModels Kotlin (Swift export ou SKIE).
