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
configurations.api.get().extendsFrom(bukkitPlugin)

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
    compileOnly("org.apache.commons:commons-lang3:3.12.0")
    api("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT")
    api("com.fasterxml.jackson.core:jackson-databind:2.12.0")

    bukkitPlugin("com.comphenix.protocol:ProtocolLib:4.6.0")

    compileOnly("org.projectlombok:lombok:1.18.4")
    annotationProcessor("org.projectlombok:lombok:1.18.4")
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
