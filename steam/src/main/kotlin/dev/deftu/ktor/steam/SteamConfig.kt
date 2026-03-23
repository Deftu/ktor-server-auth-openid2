@file:Suppress("DEPRECATION")

package dev.deftu.ktor.steam

import dev.deftu.ktor.openid2.OpenID2Config
import io.ktor.server.auth.Principal
import io.ktor.utils.io.KtorDsl

@KtorDsl
public class SteamConfig(name: String?) : OpenID2Config(name) {
    internal var authenticationFunction: suspend (SteamPrincipal) -> Principal? = { it }

    init {
        this.providerUrl = "https://steamcommunity.com/openid/login"
        validate { genericPrincipal ->
            val newPrincipal = SteamPrincipal(genericPrincipal.claimedId)
            authenticationFunction(newPrincipal)
        }
    }

    public fun validateSteam(body: suspend (SteamPrincipal) -> Principal?) {
        authenticationFunction = body
    }
}
