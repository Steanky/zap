plugins {
    id("java")
    id("java-library")
}

subprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
        }
        maven {
            url = uri("https://papermc.io/repo/repository/maven-public/")
        }
    }
}

java {
    @Suppress("UnstableApiUsage")
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

dependencies {
    api(project(":nms-common"))
    api("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT")
}

description = "arena-nms_v1_16_R3"