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
val dependencyCompileOnlyApi: Configuration by configurations.creating
val bukkitPlugin: Configuration by configurations.creating {
    isTransitive = false
}

configurations.compileOnlyApi.get().extendsFrom(dependencyCompileOnlyApi)
configurations.implementation.get().extendsFrom(shade, shadeProject)
dependencyCompileOnlyApi.extendsFrom(bukkitPlugin)

val pluginDir = "${project.properties["outputDir"] ?: "../run/server-1"}/plugins"

dependencies {
    dependencyCompileOnlyApi(project(":party", "dependencyCompileOnlyApi"))
    dependencyCompileOnlyApi(project(":party", "shadow"))

    shadeProject(project(":arena-api:nms-common")) {
        isTransitive = false
    }
    shadeProject(project(":arena-api:nms-v1_16_R3", "shadow"))
    shadeProject(project(":arena-api:nms-v1_17_R1", "reobf"))
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

val copyPlugins = tasks.register<Copy>("copyPlugins") {
    from(bukkitPlugin).into(pluginDir)
}

tasks.compileJava {
    dependsOn(copyPlugins)
}

tasks.processResources {
    expand("version" to version)
}

val relocate = tasks.register<ConfigureShadowRelocation>("relocate") {
    target = tasks.shadowJar.get()
    prefix = "io.github.zap.arenaapi.shadow"
}

tasks.shadowJar {
    dependsOn(relocate, shadeProject)

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
