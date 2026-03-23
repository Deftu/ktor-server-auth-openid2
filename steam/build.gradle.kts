plugins {
    kotlin("jvm")

    id("dev.deftu.gradle.tools")
    id("dev.deftu.gradle.tools.publishing.maven")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
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
