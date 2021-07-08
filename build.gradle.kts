plugins {
    base
}

val outputDir = System.getProperty("outputDir") ?: "./run/server-1"

tasks.clean {
    delete(fileTree("${outputDir}/plugins").matching {
        include("**/*.jar")
    }, fileTree(outputDir).matching {
        include("slimeworldmanager-classmodifier*.jar")
    })
}
