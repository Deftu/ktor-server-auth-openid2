@file:Suppress("DEPRECATION")

package dev.deftu.ktor.openid2

import io.ktor.client.HttpClient
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.auth.Principal

public open class OpenID2Config(name: String?) : AuthenticationProvider.Config(name) {
    public lateinit var client: HttpClient
    public var settings: OpenID2Settings? = null
    public var providerLookup: (suspend ApplicationCall.(OpenID2Config) -> OpenID2Settings)? = null
    public lateinit var urlProvider: suspend ApplicationCall.(OpenID2Config) -> String

    internal var authenticationFunction: suspend (OpenID2Principal) -> Principal? = { it }

    public fun validate(body: suspend (OpenID2Principal) -> Principal?) {
        authenticationFunction = body
    }

    public open fun build(): OpenID2Provider {
        require(::client.isInitialized) { "HttpClient must be initialized" }
        require(::urlProvider.isInitialized) { "urlProvider must be initialized" }
        require(settings != null || providerLookup != null) { "settings or providerLookup must be initialized" }

        return OpenID2Provider(this)
    }
}
