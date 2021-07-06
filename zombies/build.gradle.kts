plugins {
    id("java")
}

java {
    @Suppress("UnstableApiUsage")
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}


repositories {
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.rapture.pw/repository/maven-snapshots")
    maven(url = "https://repo.glaremasters.me/repository/concuncan/")
    maven(url = "https://mvn.lumine.io/repository/maven-public/")
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

    bukkitPlugin("io.lumine.xikage:MythicMobs:4.12.0")
    bukkitPlugin("com.grinderwolf:slimeworldmanager-plugin:2.6.1-SNAPSHOT")

    classModifier("com.grinderwolf:slimeworldmanager-classmodifier:2.6.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

tasks.register<Copy>("copyPlugins") {
    from(bukkitPlugin).into(pluginDir)
}

tasks.register<Copy>("copyClassModifier") {
    from(classModifier).into(outputDir)

    System.getProperty("useClassModifierVersion")?.let {
        classModifier.allDependencies.forEach {
            rename("-${it.version}", "")
        }
    }
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
    }) {
        exclude("META-INF", "META-INF/**")
    }
}

description = "zombies"
version = 1.0
