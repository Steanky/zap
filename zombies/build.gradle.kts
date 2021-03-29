plugins {
    id("java")
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
    maven {
        url = uri("https://repo.rapture.pw/repository/maven-snapshots")
    }
    maven {
        url = uri("https://repo.glaremasters.me/repository/concuncan/")
    }
    maven {
        url = uri("https://mvn.lumine.io/repository/maven-public/")
    }
}

val shade: Configuration by configurations.creating {
    isTransitive = false
}
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
    implementation(project(":arena-api"))

    shade("com.github.Steanky:RegularCommands:master-SNAPSHOT")

    bukkitPlugin("io.lumine.xikage:MythicMobs:4.11.2")
    bukkitPlugin("com.grinderwolf:slimeworldmanager-plugin:2.5.4-SNAPSHOT")

    classModifier("com.grinderwolf:slimeworldmanager-classmodifier:2.5.4-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.4")
    annotationProcessor("org.projectlombok:lombok:1.18.4")
}

tasks.register<Copy>("copyPlugins") {
    from(bukkitPlugin).into(pluginDir)
}

tasks.register<Copy>("copyClassModifier") {
    from(classModifier).into(outputDir)
}

tasks.compileJava {
    dependsOn("copyPlugins", "copyClassModifier")
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

description = "zombies"
version = 1.0
