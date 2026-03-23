@file:Suppress("DEPRECATION")

package dev.deftu.ktor.openid2

import io.ktor.client.HttpClient
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.auth.Principal
import io.ktor.utils.io.KtorDsl

@KtorDsl
public open class OpenID2Config(name: String?) : AuthenticationProvider.Config(name) {
    public lateinit var client: HttpClient

    public lateinit var providerUrl: String
    public lateinit var callbackUrl: String
    public lateinit var realm: String

    internal var authenticationFunction: suspend (OpenID2Principal) -> Principal? = { it }

    public fun validate(body: suspend (OpenID2Principal) -> Principal?) {
        authenticationFunction = body
    }

    public fun build(): OpenID2Provider {
        require(::providerUrl.isInitialized) { "Provider URL must be set" }
        require(::callbackUrl.isInitialized) { "Callback URL must be set" }
        require(::realm.isInitialized) { "Provider Realm must be set" }

        return OpenID2Provider(this)
    }
}
