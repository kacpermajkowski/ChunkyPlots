package pl.kacpermajkowski.ChunkyPlots.user;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class YamlUserRepository implements UserRepository {
    private final File userDirectory;
    private final Logger logger;

    public YamlUserRepository(File dataFolder, Logger logger) {
        this.logger = logger;
        this.userDirectory = new File(dataFolder, "users");
        if (!userDirectory.exists()) {
            userDirectory.mkdirs();
        }
    }

    @Override
    public Set<User> loadAll() {
        Set<User> users = new HashSet<>();
        File[] files = userDirectory.listFiles();
        if (files == null) {
            logger.severe("Could not load user data, which means users directory could not be created or was deleted while the plugin was loading.");
            return users;
        }

        for (File userFile : files) {
            if (userFile != null && !userFile.isDirectory()) {
                User user = loadUser(userFile);
                if (user != null) {
                    users.add(user);
                }
            }
        }
        return users;
    }

    private User loadUser(File userFile) {
        String playerUUIDstring = userFile.getName();
        UUID playerUUID;
        try {
            playerUUID = UUID.fromString(playerUUIDstring);
        } catch (Exception e) {
            logger.warning("Could not load user file: " + playerUUIDstring);
            return null;
        }

        FileConfiguration userData = YamlConfiguration.loadConfiguration(userFile);
        User user = new User(playerUUID);
        user.setTeleportOnCooldown(userData.getBoolean("isTeleportOnCooldown"));
        return user;
    }

    @Override
    public void save(User user) {
        if (user == null) return;
        String fileName = user.getPlayerUUID().toString();
        File userFile = new File(userDirectory, fileName);

        FileConfiguration userConfiguration = YamlConfiguration.loadConfiguration(userFile);
        userConfiguration.set("isTeleportOnCooldown", user.isTeleportOnCooldown());

        try {
            userConfiguration.save(userFile);
        } catch (IOException e) {
            logger.severe("User '" + user.getName() + "' save failed: " + e.getMessage());
        }
    }

    @Override
    public void saveAll(Collection<User> users) {
        for (User user : users) {
            save(user);
        }
    }
}
