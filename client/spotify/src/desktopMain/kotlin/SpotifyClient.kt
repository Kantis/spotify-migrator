import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.Json
import model.Artist
import model.FollowedArtistsResponse

class SpotifyClient(
    authCode: String,
    clientId: String,
    clientSecret: String,
    redirectURI: String,
    coroutineScope: CoroutineScope,
) {
    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
        }

    // TODO: Maybe inject this instead
    private val tokenHandler =
        TokenHandler(
            client,
            clientId,
            clientSecret,
            authCode,
            redirectURI,
            coroutineScope,
        )

    suspend fun getFollowedArtists(): List<Artist> {
        tokenHandler.hasToken.withPermit {
            return client.get("https://api.spotify.com/v1/me/following") {
                bearerAuth(tokenHandler.accessToken.value)
                url {
                    parameters.append("type", "artist")
                    parameters.append("limit", "50")
                }
            }.body<FollowedArtistsResponse>().artists.artists
        }
    }
}
