# Building nova.jar

Needs JDK 21 and Gradle (or IntelliJ, which bundles Gradle support).

1. Open `gradle.properties` and replace `fabric_version` with the Fabric API version for 1.21.11 from https://fabricmc.net/develop/
2. In this folder run:

   gradle build

   (If you don't have Gradle installed: install it once, run `gradle wrapper`, then use `./gradlew build` from then on. On Windows use `gradlew.bat build`.)

3. The mod is `build/libs/nova-1.0.0.jar`. Ignore the `-sources` jar.
4. Drop it into `.minecraft/mods` next to Fabric API, with Fabric Loader 0.18.1+ and Minecraft 1.21.11.

If the build fails with "cannot find symbol" or a missing package, the Java sources use Mojang mappings for 1.21.11 and a few names (see README.md, HUD section) may need a small tweak. Paste the error and it can be fixed.

If it says Loom `1.14-SNAPSHOT` isn't found, set `loom_version` to the Loom version shown on https://fabricmc.net/develop/.

## No installs: build on GitHub

1. Set `fabric_version` in `gradle.properties` (step 1 above).
2. Create a new GitHub repository and upload everything in this folder, including the hidden `.github` folder.
3. Open the repo's Actions tab, wait for the "build" run to finish (a few minutes), open it and download the `nova-jar` artifact.
4. Unzip it. The mod is `nova-1.0.0.jar` (skip the `-sources` jar).
