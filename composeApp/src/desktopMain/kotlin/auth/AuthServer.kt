package auth

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.channels.Channel

class AuthServer(
    private val engine: ApplicationEngine,
    val authCodeChannel: Channel<String>,
) {
    fun stop() {
        engine.stop()
    }

    companion object {
        fun create(): AuthServer {
            val authChannel = Channel<String>()

            return AuthServer(
                embeddedServer(Netty, port = 8080) {
                    authServerModule(authChannel)
                }.apply(ApplicationEngine::start),
                authChannel,
            )
        }

        private fun Application.authServerModule(authCodeChannel: Channel<String>) {
            configureRouting(authCodeChannel)
        }

        private fun Application.configureRouting(authCodeChannel: Channel<String>) {
            routing {
                route("/") {
                    get {
                        // TODO: make a nice page to return, with a close button??
                        call.respondText("Token received, you can close this tab now")
                        val authCode = call.request.queryParameters["code"] ?: error("No auth code")
                        authCodeChannel.send(authCode)
                    }
                }
            }
        }
    }
}
