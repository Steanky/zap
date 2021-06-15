plugins {
    id("java")
    id("java-library")
}

java {
    @Suppress("UnstableApiUsage")
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

repositories {
    maven(url = "https://jitpack.io")
}

val shade: Configuration by configurations.creating {
    isTransitive = false
}
val bukkitPlugin: Configuration by configurations.creating {
    isTransitive = false
}
val resolvableApi: Configuration by configurations.creating
configurations.api.get().extendsFrom(shade, bukkitPlugin, resolvableApi)

val pluginDir = "${System.getProperty("outputDir") ?: "../run/server-1"}/plugins"

dependencies {
    api(project(":party"))
    api("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT")

    shade("com.fasterxml.jackson.core:jackson-core:2.12.2")
    shade("com.fasterxml.jackson.core:jackson-databind:2.12.2")
    shade("com.fasterxml.jackson.core:jackson-annotations:2.12.2")
    shade("org.apache.commons:commons-lang3:3.12.0")

    bukkitPlugin("com.comphenix.protocol:ProtocolLib:4.6.0")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

tasks.register<Copy>("copyPlugins") {
    from(bukkitPlugin).into(pluginDir)
}

tasks.compileJava {
    dependsOn("copyPlugins")
}

tasks.processResources {
    expand("version" to version)
}

tasks.jar {
    dependsOn(resolvableApi)
    destinationDirectory.set(File(pluginDir))
    from (shade.map {
        if (it.isDirectory) it else zipTree(it)
    }) {
        exclude("META-INF", "META-INF/**", "module-info.class")
    }
}

description = "arena-api"
version = 1.0
