package pl.kacpermajkowski.ChunkyPlots.user;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class UserManager implements Listener {
	private static UserManager instance;

	private final Set<User> users = new HashSet<>();
	private final File userDirectory;
	private final Logger logger;

	public UserManager(File dataFolder, Logger logger) {
		this.logger = logger;
		this.userDirectory = new File(dataFolder, "users");
		if (!userDirectory.exists()) {
			userDirectory.mkdirs();
		}
		loadUsers();
		instance = this;
	}

	@Deprecated
	public static UserManager getInstance() {
		return instance;
	}

	private void loadUsers() {
		File[] files = userDirectory.listFiles();
		if (files == null) {
			logger.severe("Could not load user data, which means users directory could not be created or was deleted while the plugin was loading.");
			return;
		}

		for (File userFile : files) {
			if (userFile != null && !userFile.isDirectory()) {
				loadUserFromFile(userFile);
			}
		}
	}

	private void loadUserFromFile(File userFile) {
		String playerUUIDstring = userFile.getName();
		UUID playerUUID;
		try {
			playerUUID = UUID.fromString(playerUUIDstring);
		} catch (Exception e) {
			logger.warning("Could not load user file: " + playerUUIDstring);
			return;
		}

		FileConfiguration userData = YamlConfiguration.loadConfiguration(userFile);
		User user = new User(playerUUID);
		user.setTeleportOnCooldown(userData.getBoolean("isTeleportOnCooldown"));
		users.add(user);
	}

	private void createUser(OfflinePlayer player) {
		final User user = new User(player);
		users.add(user);
		saveUser(user);
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if (getUser(player) == null) {
			createUser(player);
		}
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		saveUser(event.getPlayer());
	}

	public void saveUsers() {
		for (User user : users) {
			saveUser(user);
		}
	}

	public void saveUser(OfflinePlayer player) {
		saveUser(getUser(player));
	}

	public void saveUser(User user) {
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

	public User getUser(UUID uuid) {
		for (User user : users) {
			if (user.getPlayerUUID().equals(uuid)) {
				return user;
			}
		}
		return null;
	}

	public User getUser(OfflinePlayer player) {
		return getUser(player.getUniqueId());
	}
}
