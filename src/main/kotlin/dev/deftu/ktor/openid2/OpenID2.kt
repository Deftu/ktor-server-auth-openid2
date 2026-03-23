package dev.deftu.ktor.openid2

import io.ktor.server.auth.AuthenticationConfig

public fun AuthenticationConfig.openid2(
    name: String? = null,
    configure: OpenID2Config.() -> Unit
) {
    val config = OpenID2Config(name).apply(configure)
    register(config.build())
}
