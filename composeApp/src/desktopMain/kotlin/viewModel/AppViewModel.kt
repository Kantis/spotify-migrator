package viewModel

import SpotifyClient
import SpotifyData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.AuthServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder

class AppViewModel(private val authServer: AuthServer) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun launchSpotifyAuth() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(
                URI(
                    "https://accounts.spotify.com/authorize?client_id=${SpotifyData.CLIENT_ID}&response_type=code&redirect_uri=${
                        URLEncoder.encode(
                            "http://localhost:8080",
                            Charsets.UTF_8,
                        )
                    }&scope=user-follow-read",
                ),
            )
        } else {
            error("Can't open browser")
        }

        viewModelScope.launch {
            val authCode = authServer.authCodeChannel.receive()
            _uiState.value = AppUiState(awaitingAuth = false)

            SpotifyClient(
                authCode,
                SpotifyData.CLIENT_ID,
                SpotifyData.CLIENT_SECRET,
                "http://localhost:8080",
                viewModelScope,
            ).getFollowedArtists().forEach {
                println(it)
            }
        }
    }
}

data class AppUiState(
    val awaitingAuth: Boolean = true,
)
