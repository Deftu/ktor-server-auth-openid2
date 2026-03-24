# ktor-server-auth-openid2 & ktor-server-auth-steam
A modern, lightweight Ktor 3.0+ authentication provider for OpenID 2.0 and Steam.
Since Ktor lacks native support for legacy OpenID 2.0, this library bridges the gap for gaming-focused backends.

---

[![Discord Badge](https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/cozy/social/discord-singular_64h.png)](https://s.deftu.dev/discord)
[![Ko-Fi Badge](https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/cozy/donate/kofi-singular_64h.png)](https://s.deftu.dev/kofi)

---

## Modules
- `ktor-server-auth-openid2`: The base provider for any OpenID 2.0 compliant service.
- `ktor-server-auth-steam`: A specialized wrapper with pre-configured endpoints and SteamID64 parsing.

---

## Quick Start (Steam)

### 1. Installation
Add the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://maven.deftu.dev/releases")
}

dependencies {
    implementation("dev.deftu:ktor-server-auth-steam:1.0.0")
}
```

### 2. Configuration
Install the plugin within your Ktor server.

> [!NOTE]
> Steam is extremely strict regarding the `realm` and `returnTo` (callback URL) parameters. Ensure they are consistent and correctly configured.

```kotlin
install(Authentication) {
    steam("steam") {
        client = HttpClient(CIO) // Required for server-to-server verification

        urlProvider = { "http://localhost:8080/auth/callback" }
        realm = "http://localhost:8080"

        validateSteam { principal ->
            // principal.steamId is a Long (SteamID64)
            // Perform database lookups or session creation here
            principal
        }
    }
}
```

### 3. Usage
Protect your routes with the `steam` authentication provider.

```kotlin
routing {
    authenticate("steam") {
        get("/login") {
            // Automatically redirects to Steam if not authenticated
        }

        get("/auth/callback") {
            val principal = call.principal<SteamPrincipal>()
            call.respondText("Welcome, ${principal?.steamId}!")
        }
    }
}
```

---

[![BisectHosting](https://www.bisecthosting.com/partners/custom-banners/8fb6621b-811a-473b-9087-c8c42b50e74c.png)](https://bisecthosting.com/deftu)

---

**This project is licensed under [LGPL-3.0][lgpl3].**\
**&copy; 2026 Deftu**

[lgpl3]: https://www.gnu.org/licenses/lgpl-3.0.en.html
