plugins {
    kotlin("jvm") version("2.3.20")

    val dgt = "2.75.0"
    id("dev.deftu.gradle.tools") version(dgt)
    id("dev.deftu.gradle.tools.publishing.maven") version(dgt)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth)

    testImplementation(kotlin("test"))
    implementation(libs.ktor.server.netty)
    testImplementation(libs.ktor.server.test.host)
}

java {
    withSourcesJar()
}

kotlin {
    explicitApi()
    jvmToolchain(21)
}

tasks {
    test {
        failOnNoDiscoveredTests = false
        useJUnitPlatform()
    }
}
