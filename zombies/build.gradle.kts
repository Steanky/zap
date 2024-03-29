import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

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
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://repo.rapture.pw/repository/maven-snapshots")
    maven("https://repo.glaremasters.me/repository/concuncan/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

val shade: Configuration by configurations.creating
val shadeProject: Configuration by configurations.creating
val bukkitPlugin: Configuration by configurations.creating {
    isTransitive = false
}
val classModifier: Configuration by configurations.creating {
    isTransitive = false
}

configurations.implementation.get().extendsFrom(shade, shadeProject, bukkitPlugin)

val outputDir = project.properties["outputDir"] ?: "../run/server-1"
val pluginDir = "$outputDir/plugins"

dependencies {
    implementation(project(":arena-api", "dependencyApi"))
    implementation(project(":arena-api", "shadow"))
    implementation("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    implementation("com.grinderwolf:slimeworldmanager-api:2.6.1-SNAPSHOT")

    shadeProject(project(":zombies:nms-common")) {
        isTransitive = false
    }
    shadeProject(project(":zombies:nms-v1_16_R3", "shadow"))

    shade("com.github.Steanky:RegularCommands:master-SNAPSHOT")

    bukkitPlugin("io.lumine.xikage:MythicMobs:4.12.0-Fixed")
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

    project.properties["useClassModifierVersion"]?.let {
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
    prefix = "io.github.zap.zombies.shadow"
}

tasks.shadowJar {
    dependsOn(relocate.get())

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

description = "zombies"
version = 1.0
