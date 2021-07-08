plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

repositories {
    mavenLocal()
    maven("https://libraries.minecraft.net")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

val outputJar: Configuration by configurations.creating {
    isCanBeResolved = false
}

dependencies {
    api(project(":nms-common"))
    api("com.destroystokyo.paper:paper:1.16.5-R0.1-SNAPSHOT") {
        exclude("io.papermc", "minecraft-server")
    }
}

artifacts {
    add("outputJar", tasks.jar.get())
}

description = "arena-nms_v1_16_R3"
