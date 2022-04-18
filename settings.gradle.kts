rootProject.name = "root"
include(":vector", ":arena-api:nms-common", ":arena-api:nms-v1_16_R3", "arena-api:nms-v1_17_R1",
    ":arena-api", ":zombies:nms-common", ":zombies:nms-v1_16_R3", ":zombies:nms-v1_17_R1", ":zombies", ":party")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}
