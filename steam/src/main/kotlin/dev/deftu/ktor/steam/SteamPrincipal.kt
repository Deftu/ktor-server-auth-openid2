package dev.deftu.ktor.steam

import dev.deftu.ktor.openid2.OpenID2Principal

public class SteamPrincipal(claimedId: String) : OpenID2Principal(claimedId) {
    public val steamId: Long by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val idPart = claimedId.substringAfterLast("/").substringBefore("?")
        idPart.toLongOrNull() ?: throw IllegalArgumentException("Invalid SteamID in claimedId: $claimedId")
    }
}
