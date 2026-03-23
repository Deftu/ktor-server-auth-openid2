package dev.deftu.ktor.openid2

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(Netty, 8080) {
        install(Authentication) {
            openid2("steam") {
                client = HttpClient(CIO)
                providerUrl = "https://steamcommunity.com/openid/login"
                callbackUrl = "http://localhost:8080/auth/callback"
                realm = "http://localhost:8080"
            }
        }

        routing {
            get("/") {
                call.respondText("Public Home Page. Go to /login to start.")
            }

            // This route triggers the 'challenge' and redirects to Steam
            authenticate("steam") {
                get("/login") {
                    // If we reach here, we are already authenticated
                    val principal = call.principal<OpenID2Principal>()
                    call.respondText("Redirecting logic worked! Hello, ${principal?.claimedId}")
                }

                // This is where Steam sends the user back
                get("/auth/callback") {
                    val principal = call.principal<OpenID2Principal>()
                    call.respondText("Successfully logged in! SteamID: ${principal?.claimedId}")
                }
            }
        }
    }.start(wait = true)
}
