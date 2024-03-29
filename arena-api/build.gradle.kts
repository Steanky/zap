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
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

val shade: Configuration by configurations.creating
val shadeProject: Configuration by configurations.creating
val dependencyApi: Configuration by configurations.creating
val bukkitPlugin: Configuration by configurations.creating {
    isTransitive = false
}

configurations.api.get().extendsFrom(dependencyApi)
configurations.implementation.get().extendsFrom(shade, shadeProject)
dependencyApi.extendsFrom(bukkitPlugin)

val pluginDir = "${project.properties["outputDir"] ?: "../run/server-1"}/plugins"

dependencies {
    dependencyApi(project(":party", "dependencyApi"))
    dependencyApi(project(":party", "shadow"))

    shadeProject(project(":arena-api:nms-common")) {
        isTransitive = false
    }
    shadeProject(project(":arena-api:nms-v1_16_R3", "shadow"))
    shadeProject(project(":vector")) {
        isTransitive = false
    }
    shade("com.fasterxml.jackson.core:jackson-core:2.12.3")
    shade("com.fasterxml.jackson.core:jackson-databind:2.12.3")
    shade("com.fasterxml.jackson.core:jackson-annotations:2.12.3")
    shade("org.apache.commons:commons-lang3:3.12.0")

    bukkitPlugin("com.comphenix.protocol:ProtocolLib:4.7.0")

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
    dependsOn(relocate.get(), shadeProject)

    configurations = listOf(shade)
    archiveClassifier.set("")
    destinationDirectory.set(File(pluginDir))

    from(shadeProject.map {
        if (it.isDirectory) it else zipTree(it)
    })
}

tasks.build {
    dependsOn(tasks.shadowJar.get())
}

description = "arena-api"
version = 1.0
