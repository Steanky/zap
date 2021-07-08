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
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

val shade: Configuration by configurations.creating
val dependencyApi: Configuration by configurations.creating
val bukkitPlugin: Configuration by configurations.creating {
    isTransitive = false
}

configurations.api.get().extendsFrom(dependencyApi)
configurations.implementation.get().extendsFrom(shade)
dependencyApi.extendsFrom(bukkitPlugin)

val pluginDir = "${System.getProperty("outputDir") ?: "../run/server-1"}/plugins"

dependencies {
    dependencyApi(project(":party", "dependencyApi"))
    dependencyApi(project(":party", "shadow"))

    shade(project(":nms-common", "outputJar"))
    shade(project(":nms-v1_16_R3", "shadow"))
    shade(project(":vector", "outputJar"))
    shade("com.fasterxml.jackson.core:jackson-core:2.12.3")
    shade("com.fasterxml.jackson.core:jackson-databind:2.12.3")
    shade("com.fasterxml.jackson.core:jackson-annotations:2.12.3")
    shade("org.apache.commons:commons-lang3:3.12.0")

    bukkitPlugin("com.comphenix.protocol:ProtocolLib:4.6.0")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

val copyPlugins = tasks.register<Copy>("copyPlugins") {
    from(bukkitPlugin).into(pluginDir)
}

tasks.compileJava {
    dependsOn(copyPlugins.get())
}

tasks.processResources {
    expand("version" to version)
}

val relocate = tasks.register<ConfigureShadowRelocation>("relocate") {
    target = tasks.shadowJar.get()
    prefix = "io.github.zap.arenaapi.shadow"
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

description = "arena-api"
version = 1.0
