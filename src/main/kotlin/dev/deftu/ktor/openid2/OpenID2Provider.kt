package dev.deftu.ktor.openid2

import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.isSuccess
import io.ktor.http.parametersOf
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.response.respondRedirect
import io.ktor.util.filter

public class OpenID2Provider(private val config: OpenID2Config) : AuthenticationProvider(config) {
    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call
        val provider = config.providerLookup?.invoke(call, config)
            ?: config.settings
            ?: return context.error("OpenID2", AuthenticationFailedCause.Error("No provider settings found"))
        val params = call.request.queryParameters
        val mode = params["openid.mode"]

        when (mode) {
            // No mode means this is the initial request, so we should redirect to the provider
            null -> {
                context.challenge("OpenID2", AuthenticationFailedCause.NoCredentials) { challenge, call ->
                    call.respondRedirect(buildDiscoveryUrl(call, provider))
                    challenge.complete()
                }
            }

            // The provider is redirecting back to us with the assertion, so we should verify it
            "id_res" -> {
                if (verifyAssertion(provider, params)) {
                    val claimedId = params["openid.claimed_id"] ?: return context.error("OpenID2", AuthenticationFailedCause.Error("Missing claimed_id in response"))
                    val principal = OpenID2Principal(claimedId)
                    val checkedPrincipal = config.authenticationFunction(principal)
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
    private suspend fun buildDiscoveryUrl(call: ApplicationCall, settings: OpenID2Settings): String {
        return URLBuilder(settings.providerUrl).apply {
            parameters.appendAll(parametersOf(
                "openid.ns" to listOf("http://specs.openid.net/auth/2.0"),
                "openid.mode" to listOf("checkid_setup"),
                "openid.return_to" to listOf(config.urlProvider(call, config)),
                "openid.realm" to listOf(settings.realm),
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

    private suspend fun verifyAssertion(settings: OpenID2Settings, params: Parameters): Boolean {
        val verificationParams = Parameters.build {
            appendAll(params.filter { key, _ -> key.startsWith("openid.") })
            set("openid.mode", "check_authentication")
        }

        val response = config.client.post(settings.providerUrl) {
            setBody(FormDataContent(verificationParams))
        }

        if (!response.status.isSuccess()) {
            return false
        }

        val responseText = response.bodyAsText()
        return parseKeyValueForm(responseText)["is_valid"] == "true"
    }
}
