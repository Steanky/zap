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
    mavenLocal()
    maven("https://libraries.minecraft.net")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

val shade: Configuration by configurations.creating

configurations.implementation.get().extendsFrom(shade)

dependencies {
    implementation(project(":nms-common"))
    implementation("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT") {
        exclude("io.papermc", "minecraft-server")
    }
    shade("org.apache.commons:commons-lang3:3.12.0")
}

val relocate = tasks.register<ConfigureShadowRelocation>("relocate") {
    target = tasks.shadowJar.get()
    prefix = "io.github.zap.nms.v1_16_R3.shadow"
}

tasks.shadowJar {
    dependsOn(relocate.get())

    configurations = listOf(shade)
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar.get())
}

description = "arena-nms_v1_16_R3"
