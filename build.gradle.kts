plugins {
    id("base")
}

subprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
        }
    }
}

val outputDir = System.getProperty("outputDir") ?: "./run/server-1"

tasks.getByName<Delete>("clean") {
    delete(fileTree("${outputDir}/plugins").matching {
        include("**/*.jar")
    }, fileTree(outputDir).matching {
        include("slimeworldmanager-classmodifier*.jar")
    })
}
