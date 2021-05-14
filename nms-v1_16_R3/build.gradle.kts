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

dependencies {
    api(project(":nms-common"))
    api("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT")
}

description = "arena-nms_v1_16_R3"