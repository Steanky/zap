# zombies-plugin
A plugin intended to recreate a certain Hypixel gamemode, but with more features, maps, a more up-to-date Minecraft version, less bugs, and an active development team. This software is intended to be hosted and run by the development team themselves.

### Building
To build this project given the current pom.xml, you will need to supply the directory of the plugins folder contained in whatever local PaperMC server you are using for debugging. To do this on the Maven command line, simply supply the argument "-DpluginDir=path-to-plugin-folder". Example:

`mvn clean compile package "-DpluginDir=C:\Users\Owner\Desktop\Minecraft Files\Modding\Test Server\plugins"`

This is designed to make testing very easy. You can simply create a Build/Run configuration in IntelliJ from the "Jar Application" template, supply the relevant arguments, and then start the server with a single click. Setup instructions:

1. Go to `Edit Configurations...` in the dropdown next to the `Run` button
2. Create a new configuration from the `JAR Application` template
3. Under the configuration tab:
   1. Set `Path to Jar` to the path to the location of your PaperMC build
   2. Add the `-nogui` flag to `Program Arguments`
   3. Set `Working directory` to the directory that your PaperMC build is in.
4. Under the `Before launch` task list:
   1. Add a new `Run Maven Goal` task (click the plus button)
   2. Keeping the working directory set to default (it should point to your IntelliJ project folder), set `Command line` to `clean compile package "-DpluginDir=path-to-plugins-folder"`, replacing `path-to-plugins-folder` with the path to the plugins folder used by your test server
5. At this point, you should be done. Make sure your configuration is selected, and then run. IntelliJ should start your test server after Maven finishes cleaning, compiling, and finally packaging the plugin to the appropriate folder.

### Contributing
Your general workflow should follow these steps:

1. When you want to work on a new feature or component, make new branch.
2. Commit and push your changes to the branch periodically.
3. When you think you're done, submit a pull request (merge to main).
4. The other coders will review and comment on your code, either on github or (preferably) Discord.
5. Make edits based on the feedback you receive.
6. Push your edits.
7. Once your request is approved, your edits will be merged to main.
