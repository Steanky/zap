import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
    id("io.papermc.paperweight.userdev") version "1.1.11"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnlyApi(project(":zombies:nms-common"))

    paperDevBundle("1.17.1-R0.1-SNAPSHOT")

    implementation("org.apache.commons:commons-lang3:3.12.0")
}

val relocate = tasks.register<ConfigureShadowRelocation>("relocate") {
    target = tasks.shadowJar.get()
    prefix = "io.github.zap.zombies.nms.v1_17_R1.shadow"
}

tasks.shadowJar {
    dependsOn(relocate)

    archiveClassifier.set("")

    finalizedBy(tasks.reobfJar.get())
}

tasks.build {
    dependsOn(tasks.shadowJar.get())
}

description = "arena-nms_v1_17_R1"
