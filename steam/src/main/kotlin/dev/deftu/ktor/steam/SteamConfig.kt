@file:Suppress("DEPRECATION")

package dev.deftu.ktor.steam

import dev.deftu.ktor.openid2.OpenID2Config
import dev.deftu.ktor.openid2.OpenID2Provider
import dev.deftu.ktor.openid2.OpenID2Settings
import io.ktor.server.auth.Principal

public class SteamConfig(name: String?) : OpenID2Config(name) {
    public companion object {
        public const val PROVIDER_URL: String = "https://steamcommunity.com/openid/login"
    }

    public var realm: String? = null

    internal var authenticationFunction: suspend (SteamPrincipal) -> Principal? = { it }

    init {
        validate { genericPrincipal ->
            val newPrincipal = SteamPrincipal(genericPrincipal.claimedId)
            authenticationFunction(newPrincipal)
        }
    }

    public fun validateSteam(body: suspend (SteamPrincipal) -> Principal?) {
        authenticationFunction = body
    }

    override fun build(): OpenID2Provider {
        require(realm != null || settings != null || providerLookup != null) { "realm, settings, or providerLookup must be initialized" }

        if (this.realm != null && this.settings == null && this.providerLookup == null) {
            this.settings = OpenID2Settings(PROVIDER_URL, realm!!)
        }

        return super.build()
    }
}
