plugins {
    id("java")
    id("java-library")
}

java {
    @Suppress("UnstableApiUsage")
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    maven {
        url = uri("https://jitpack.io")
    }
}

val shade: Configuration by configurations.creating {
    isTransitive = false
}
val bukkitPlugin: Configuration by configurations.creating {
    isTransitive = false
}
configurations.api.get().extendsFrom(shade, bukkitPlugin)

val pluginDir = "${System.getProperty("outputDir") ?: "../run/server-1"}/plugins"

dependencies {
    api("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT")
    shade("com.github.Steanky:RegularCommands:master-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.4")
    annotationProcessor("org.projectlombok:lombok:1.18.4")
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
    destinationDirectory.set(File(pluginDir))
    from (shade.map {
        if (it.isDirectory) it else zipTree(it)
    })
}

description = "party++"
version = 1.0
