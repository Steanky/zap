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
    api("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
}

description = "nms-common"