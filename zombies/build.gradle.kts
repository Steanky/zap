import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import java.io.File

plugins {
    java
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
    maven("https://repo.rapture.pw/repository/maven-snapshots")
    maven("https://repo.glaremasters.me/repository/concuncan/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

val shade: Configuration by configurations.creating

val bukkitPlugin: Configuration by configurations.creating {
    isTransitive = false
}

val classModifier: Configuration by configurations.creating {
    isTransitive = false
}

configurations.implementation.get().extendsFrom(shade, bukkitPlugin, classModifier)

val outputDir = System.getProperty("outputDir") ?: "../run/server-1"
val pluginDir = "$outputDir/plugins"

dependencies {
    implementation(project(":arena-api", "dependencyApi"))
    implementation(project(":arena-api", "shadow"))

    shade("com.github.Steanky:RegularCommands:master-SNAPSHOT")

    bukkitPlugin("io.lumine.xikage:MythicMobs:4.12.0")
    bukkitPlugin("com.grinderwolf:slimeworldmanager-plugin:2.6.1-SNAPSHOT")

    classModifier("com.grinderwolf:slimeworldmanager-classmodifier:2.6.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

val copyPlugins = tasks.register<Copy>("copyPlugins") {
    from(bukkitPlugin).into(pluginDir)
}

val copyClassModifier = tasks.register<Copy>("copyClassModifier") {
    from(classModifier).into(outputDir)

    System.getProperty("useClassModifierVersion")?.let {
        classModifier.allDependencies.forEach {
            rename("-${it.version}", "")
        }
    }
}

tasks.compileJava {
    dependsOn(copyPlugins.get(), copyClassModifier.get())
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

description = "zombies"
version = 1.0
