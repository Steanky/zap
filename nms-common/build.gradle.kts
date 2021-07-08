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
val outputJar: Configuration by configurations.creating {
    isCanBeResolved = false
}

configurations.api.get().extendsFrom(bukkitPlugin)

dependencies {
    api("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    api(project(":vector"))

    bukkitPlugin("com.comphenix.protocol:ProtocolLib:4.6.0")
}

artifacts {
    add("outputJar", tasks.jar.get())
}

description = "nms-common"
