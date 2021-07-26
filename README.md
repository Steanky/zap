# ZAP [![Discord](https://img.shields.io/discord/289587909051416579.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/JvKsGeKjW9)
---
Recreation of the Hypixel Arcade minigame Zombies.

# Requirements
---
[Maven](https://maven.apache.org/) is required to build the project. The build tool used, however, is gradle 7.1.1. Maven is used for its local repository.
As we support Minecraft 1.16+, the latest LTS version of Java is required. For the moment, JDK 16 is required.

# Building Paper
---
We need to build [Paper](https://github.com/PaperMC/Paper) to build our plugin.
Installation steps are as follows:

Minecraft 1.16.5:
```
1. Download the latest Paper 1.16.5 build from https://papermc.io/downloads#Paper-1.16
2. Run java -Dpaperclip.install=true -jar paperclip.jar, where paperclip.jar is the newly downloaded jar.
```

Minecraft 1.17+: (Note that these versions will take a while to set up! Consider installing WSL if you are on Windows to speed up the build process.)
```
1. git clone https://github.com/PaperMC/paperweight.git
2. git checkout userdev (unless this branch has been merged, then stay on master)
3. ./gradlew publishToMavenLocal

4. git clone https://github.com/PaperMC/Paper.git
5. Checkout the branch of the version you are building. If this is the latest version, it will be the branch userdev. If it is an old version, checkout its branch. For example, ver/1.16.5 (not applicable to this section, assuming userdev will have been merged by 1.18).
6. gradlew applyPatches
7. gradlew publishToMavenLocal -PpublishDevBundle
```

# Building
---
Execute `gradlew build` in the root directory to build the project. (If you have upgraded a plugin dependency, do gradlew clean build). This will automatically output to the `./run/server-1/` directory for the slime world manager classmodifier and to `./run/server-1/plugins` for all plugins. This can be configured by adding the -PoutputDir parameter to the build task and choosing a new directory.

# Running
---
In your server directory, using a Paper server jar called `server.jar` and slime world manager classmodifier called `slimeworldmanager-classmodifier.jar`, you can run the server using `java -javaagent:slimeworldmanager-classmodifier.jar -jar server.jar`. Adjust arguments as necessary.

# Guidelines
---
We are not too strict on our guidelines. Usage of JetBrains @NotNull and @Nullable annotations are encouraged. Your code should adhere to typical Java conventions for the most part (use your own judgement).
Commits to the `unstable` branch are regularly merged and should only be for quick small fixes. All other major feature changes should be submitted as Pull Requests to the `main` branch.
Please review other contributors' Pull Requests. Ideally, multiple people should review large pull requests before merging.
