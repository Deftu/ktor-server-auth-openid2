package dev.deftu.ktor.steam

import io.ktor.server.auth.AuthenticationConfig

public fun AuthenticationConfig.steam(
    name: String? = null,
    configure: SteamConfig.() -> Unit
) {
    val config = SteamConfig(name).apply(configure)
    register(config.build())
}
