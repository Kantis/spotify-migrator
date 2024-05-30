import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.http.parameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

class TokenHandler(
    client: HttpClient,
    private val clientId: String,
    private val clientSecret: String,
    private val initialAuthCode: String,
    private val redirectURI: String,
    coroutineScope: CoroutineScope,
) {
    val hasToken: Semaphore = Semaphore(1, acquiredPermits = 1)
    val accessToken: MutableStateFlow<String> = MutableStateFlow("")

    init {
        coroutineScope.launch {
            var authResponse = client.authenticate()
            hasToken.release()
            while (isActive) {
                accessToken.value = authResponse.accessToken
                delay(authResponse.expiresIn.seconds - REFRESH_TOKEN_MARGIN)
                logger.info("Token expiring in ${REFRESH_TOKEN_MARGIN.inWholeSeconds} seconds, refreshing token...")
                authResponse = client.refreshToken(authResponse.refreshToken)
            }
        }
    }

    private suspend fun HttpClient.authenticate(): AuthResponse =
        submitForm(
            url = SPOTIFY_TOKEN_URL,
            formParameters =
                parameters {
                    append("grant_type", "authorization_code")
                    append("code", initialAuthCode)
                    append("redirect_uri", redirectURI)
                },
        ) {
            basicAuth(clientId, clientSecret)
        }.body()

    private suspend fun HttpClient.refreshToken(refreshToken: String): AuthResponse =
        submitForm(
            url = SPOTIFY_TOKEN_URL,
            formParameters =
                parameters {
                    append("grant_type", "refresh_token")
                    append("refresh_token", refreshToken)
                },
        ) {
            basicAuth(clientId, clientSecret)
        }.body()

    @Serializable
    private data class AuthResponse(
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("token_type")
        val tokenType: String,
        @SerialName("expires_in")
        val expiresIn: Int,
        @SerialName("refresh_token")
        val refreshToken: String,
        val scope: String,
    )

    companion object {
        /**
         * The margin to subtract from the token's expiration time to ensure we refresh it in time.
         */
        private val REFRESH_TOKEN_MARGIN = 10.seconds

        private const val SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token"

        private val logger = LoggerFactory.getLogger(TokenHandler::class.java)
    }
}
