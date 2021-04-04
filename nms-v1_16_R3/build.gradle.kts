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
    implementation(project(":nms-common"))
    implementation("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT")
}

description = "arena-nms_v1_16_R3"