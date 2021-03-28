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

val bukkitPlugin: Configuration by configurations.creating {
    isTransitive = false
}
val shade: Configuration by configurations.creating {
    isTransitive = false
}
configurations.api.get().extendsFrom(bukkitPlugin, shade)

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://maven.springframework.org/release")
    }
}

dependencies {
    api("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT")
    shade("com.fasterxml.jackson.core:jackson-core:2.12.2")
    shade("com.fasterxml.jackson.core:jackson-databind:2.12.2")
    shade("com.fasterxml.jackson.core:jackson-annotations:2.12.2")
    shade("org.apache.commons:commons-lang3:3.12.0")

    bukkitPlugin("com.comphenix.protocol:ProtocolLib:4.6.0")

    compileOnly("org.projectlombok:lombok:1.18.4")
    annotationProcessor("org.projectlombok:lombok:1.18.4")
}

tasks.withType<Jar> {
    destinationDirectory.set(File(System.getProperty("outputDir") ?: "../run/server-1/plugins"))
    from (shade.map {
        if (it.isDirectory) it else zipTree(it)
    })
}

tasks.register<Copy>("copyPlugins") {
    from(bukkitPlugin).into("../run/server-1/plugins")
    bukkitPlugin.allDependencies.forEach {
        rename("-${it.version}", "")
    }
}

tasks.compileJava {
    dependsOn("copyPlugins")
}

description = "arena-api"
