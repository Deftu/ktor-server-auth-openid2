@file:Suppress("DEPRECATION")

package dev.deftu.ktor.openid2

import io.ktor.server.auth.Principal

public open class OpenID2Principal(public val claimedId: String) : Principal
