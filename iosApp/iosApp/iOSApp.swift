import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        // Démarre Koin une seule fois, avant la création de la première vue Compose.
        MainViewControllerKt.setupKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
