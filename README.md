
## Local Development Setup - Windows
1. Clone the repository:
   ```sh
   git clone https://github.com/kacpermajkowski/ChunkyPlots.git
   cd ChunkyPlots
   ```
2. Setup your local Spigot development server for newest stable Minecraft version and configure your startup script to automatically restart when server is stopped.

    ```bat
    :main
    java -Xms3G -Xmx3G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -jar paper.jar --nogui
    goto main
    ```
3. Enable RCON in `server.properties`:
    ```yaml
    enable-rcon=true
   ...
    rcon.password=<choose your RCON password here>
    rcon.port=25575
    ```
   
4. Create `config.properties` file inside the project folder and provide your dev server's `plugins` directory path and RCON credentials.

    ```yaml
    plugins.dir=<your dev server's `plugins` directory path>
    rcon.password=<insert your RCON password here>
    rcon.port=25575
    ```

5. Download [mcrcon](https://github.com/Tiiffi/mcrcon/releases/) and unzip it anywhere you like (e.g. `C:\Program Files\mcrcon`). Make sure `mcrcon.exe` is directly in the directory you chose. Copy the directory's path.
6. Add the path to `PATH` environment variable. Restart your IDE and terminal.

7. Run `mvn install`.
8. Run your dev server's startup script.
9. Check if `ChunkyPlots` is present in `plugins` command output and if so, it's done :D

Now every time you run `mvn install` after the server has already been running, it'll first copy the new JAR into the plugins folder and then stop the server so that it'll automatically restart.