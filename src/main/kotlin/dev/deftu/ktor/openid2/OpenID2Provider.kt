package dev.deftu.ktor.openid2

import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.isSuccess
import io.ktor.http.parametersOf
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.response.respondRedirect
import io.ktor.util.filter

public class OpenID2Provider(config: OpenID2Config) : AuthenticationProvider(config) {
    private val httpClient = config.client
    private val providerUrl = config.providerUrl
    private val callbackUrl = config.callbackUrl
    private val realm = config.realm
    private val authenticationFunction = config.authenticationFunction

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call
        val params = call.request.queryParameters
        val mode = params["openid.mode"]

        when (mode) {
            // No mode means this is the initial request, so we should redirect to the provider
            null -> {
                context.challenge("OpenID2", AuthenticationFailedCause.NoCredentials) { challenge, call ->
                    call.respondRedirect(buildDiscoveryUrl())
                    challenge.complete()
                }
            }

            // The provider is redirecting back to us with the assertion, so we should verify it
            "id_res" -> {
                if (verifyAssertion(params)) {
                    val claimedId = params["openid.claimed_id"] ?: return
                    val principal = OpenID2Principal(claimedId)
                    val checkedPrincipal = authenticationFunction(principal)
                    if (checkedPrincipal != null) {
                        context.principal(checkedPrincipal)
                    }
                }
            }

            // The user cancelled the authentication, so we should fail with an appropriate message
            "cancel" -> {
                context.error("OpenID2", AuthenticationFailedCause.Error("Authentication cancelled by user"))
            }

            // We don't know what happened so pass the OpenID error along
            "error" -> {
                val errorMessage = params["openid.error"] ?: "Unknown OpenID Error"
                context.error("OpenID2", AuthenticationFailedCause.Error(errorMessage))
            }
        }
    }

    @Suppress("HttpUrlsUsage")
    private fun buildDiscoveryUrl(): String {
        return URLBuilder(providerUrl).apply {
            parameters.appendAll(parametersOf(
                "openid.ns" to listOf("http://specs.openid.net/auth/2.0"),
                "openid.mode" to listOf("checkid_setup"),
                "openid.return_to" to listOf(callbackUrl),
                "openid.realm" to listOf(realm),
                "openid.identity" to listOf("http://specs.openid.net/auth/2.0/identifier_select"),
                "openid.claimed_id" to listOf("http://specs.openid.net/auth/2.0/identifier_select"),
            ))
        }.buildString()
    }

    private fun parseKeyValueForm(text: String): Map<String, String> {
        return text.lines()
            .filter { it.contains(":") }
            .associate {
                val (key, value) = it.split(":", limit = 2)
                key.trim() to value.trim()
            }
    }

    private suspend fun verifyAssertion(params: Parameters): Boolean {
        val verificationParams = Parameters.build {
            appendAll(params.filter { key, _ -> key.startsWith("openid.") })
            set("openid.mode", "check_authentication")
        }

        val response = httpClient.post(providerUrl) {
            setBody(FormDataContent(verificationParams))
        }

        if (!response.status.isSuccess()) {
            return false
        }

        val responseText = response.bodyAsText()
        return parseKeyValueForm(responseText)["is_valid"] == "true"
    }
}
