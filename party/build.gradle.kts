import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://papermc.io/repo/repository/maven-public/")
}

val dependencyCompileOnlyApi: Configuration by configurations.creating

configurations.compileOnlyApi.get().extendsFrom(dependencyCompileOnlyApi)

val pluginDir = "${project.properties["outputDir"] ?: "../run/server-1"}/plugins"

dependencies {
    dependencyCompileOnlyApi("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT") {
        exclude("junit", "junit")
    }
    implementation("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT") {
        exclude("net.kyori", "adventure-api")
    }
    implementation("com.github.Steanky:RegularCommands:master-SNAPSHOT")
    implementation("org.apache.commons:commons-lang3:3.12.0")

    testRuntimeOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT") {
        exclude("junit", "junit")
    }

    testImplementation("org.mockito:mockito-core:3.11.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.0-M1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.processResources {
    expand("version" to version)
}

val relocate = tasks.register<ConfigureShadowRelocation>("relocate") {
    target = tasks.shadowJar.get()
    prefix = "io.github.zap.party.shadow"
}

tasks.shadowJar {
    dependsOn(relocate.get())

    archiveClassifier.set("")
    destinationDirectory.set(File(pluginDir))
}

tasks.build {
    dependsOn(tasks.shadowJar.get())
}

description = "party-plus-plus"
version = 1.0
