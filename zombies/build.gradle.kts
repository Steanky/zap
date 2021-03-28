plugins {
    id("java")
}

java {
    @Suppress("UnstableApiUsage")
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://maven.springframework.org/release")
    }
}

val bukkitPlugin: Configuration by configurations.creating {
    isTransitive = false
}
val classModifier: Configuration by configurations.creating {
    isTransitive = false
}
configurations.compileOnly.get().extendsFrom(bukkitPlugin, classModifier)

dependencies {
    implementation("com.github.Steanky:RegularCommands:master-SNAPSHOT")
    compileOnly(project(":arena-api"))

    bukkitPlugin("io.lumine.xikage:MythicMobs:4.11.2")
    bukkitPlugin("com.grinderwolf:slimeworldmanager-plugin:2.5.4-SNAPSHOT")

    classModifier("com.grinderwolf:slimeworldmanager-classmodifier:2.5.4-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.4")
    annotationProcessor("org.projectlombok:lombok:1.18.4")
}

project.configurations.implementation.get().isCanBeResolved = true
tasks.withType<Jar> {
    from (configurations.implementation.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
}

tasks.register<Copy>("copyPlugins") {
    from(bukkitPlugin).into("../run/server-1/plugins")
    bukkitPlugin.allDependencies.forEach {
        rename("-${it.version}", "")
    }
}

tasks.register<Copy>("copyClassModifier") {
    from(classModifier).into("../run/server-1")
    classModifier.allDependencies.forEach {
        rename("-${it.version}", "")
    }
}

tasks.compileJava {
    dependsOn("copyPlugins", "copyClassModifier")
}

description = "zombies"
