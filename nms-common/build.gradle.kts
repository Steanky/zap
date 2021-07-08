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
}

val outputJar: Configuration by configurations.creating {
    isCanBeResolved = false
}

dependencies {
    api("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    api(project(":vector"))
}

artifacts {
    add("outputJar", tasks.jar.get())
}

description = "nms-common"
