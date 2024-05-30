import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import auth.AuthServer
import org.jetbrains.compose.ui.tooling.preview.Preview
import viewModel.AppViewModel

@Composable
@Preview
fun App(
    authServer: AuthServer,
    viewModel: AppViewModel = viewModel { AppViewModel(authServer) },
) {
    MaterialTheme {
        if (viewModel.uiState.value.awaitingAuth) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    viewModel.launchSpotifyAuth()
                }) {
                    Text("Authenticate with Spotify")
                }
            }
        } else {
            Text("Authenticated with Spotify")
        }
    }
}
