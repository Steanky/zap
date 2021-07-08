import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import java.io.File

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
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

val shade: Configuration by configurations.creating
val dependencyApi: Configuration by configurations.creating

configurations.implementation.get().extendsFrom(shade)
configurations.api.get().extendsFrom(dependencyApi)

val pluginDir = "${System.getProperty("outputDir") ?: "../run/server-1"}/plugins"

dependencies {
    dependencyApi("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT") {
        exclude("io.papermc", "minecraft-server")
    }
    shade("com.github.Steanky:RegularCommands:master-SNAPSHOT")
    shade("org.apache.commons:commons-lang3:3.12.0")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
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

    configurations = listOf(shade)
    archiveClassifier.set("")
    destinationDirectory.set(File(pluginDir))
}

tasks.build {
    dependsOn(tasks.shadowJar.get())
}

description = "party-plus-plus"
version = 1.0
