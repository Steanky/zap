plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

val bukkitPlugin: Configuration by configurations.creating {
    isTransitive = false
}

configurations.compileOnlyApi.get().extendsFrom(bukkitPlugin)

dependencies {
    compileOnlyApi("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT") {
        exclude("junit", "junit")
    }
    compileOnlyApi(project(":vector"))

    bukkitPlugin("com.comphenix.protocol:ProtocolLib:4.7.0")
}

description = "arena-nms-common"
