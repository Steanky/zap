plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
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

dependencies {
    compileOnlyApi(project(":zombies:nms-common"))

    compileOnly("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT") {
        exclude("io.papermc", "minecraft-server")
    }

    implementation("org.apache.commons:commons-lang3:3.12.0")
}

val relocate = tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation>("relocate") {
    target = tasks.shadowJar.get()
    prefix = "io.github.zap.zombies.nms.v1_16_R3.shadow"
}

tasks.shadowJar {
    dependsOn(relocate.get())

    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar.get())
}

description = "zombies-nms-v1_16_R3"
