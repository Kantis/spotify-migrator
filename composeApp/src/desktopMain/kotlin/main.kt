import androidx.compose.ui.window.Window
import androidx.compose.ui.window.awaitApplication
import auth.AuthServer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun main() {
    coroutineScope {
        val authServer = AuthServer.create()
        launch {
            awaitApplication {
                Window(
                    onCloseRequest = {
                        authServer.stop()
                        exitApplication()
                    },
                    title = "SpotifyMigrator",
                ) {
                    App(authServer)
                }
            }
        }
    }
}
