package pl.kacpermajkowski.ChunkyPlots.user;


import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserManager implements Listener {
	private static UserManager instance;

	private final Set<User> users = new HashSet<>();

	private UserManager(){
		loadUsers();
	}

	private void loadUsers(){
		File[] files = getUserDirectory().listFiles();
		if(files == null) {
			Bukkit.getLogger().severe("Could not load user data, which means users directory could not be created or was deleted while the plugin was loading.");
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

		} catch (Exception e){
			Bukkit.getLogger().warning("Could not load user file: " + playerUUIDstring);
			return;
		}

		FileConfiguration userData = YamlConfiguration.loadConfiguration(userFile);

		User user = new User(playerUUID);
		user.setTeleportOnCooldown(userData.getBoolean("isTeleportOnCooldown"));

		users.add(user);
	}

	private void createUser(OfflinePlayer player){
		final User user = new User(player);
		users.add(user);
		saveUser(user);
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event){
		final Player player = event.getPlayer();
		if(getUser(player) == null){
			createUser(player);
		}
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event){
		saveUser(event.getPlayer());
	}

	@EventHandler
	public void onPluginDisable(final PluginDisableEvent event){
		saveUsers();
	}

	public void saveUsers(){
		for(User user:users) {
			saveUser(user);
		}
	}

	public void saveUser(OfflinePlayer player){
		saveUser(getUser(player));
	}

	public void saveUser(User user) {
		String fileName = user.getPlayerUUID().toString();
		File userFile = new File(getUserDirectory() + "/" + fileName);

		FileConfiguration userConfiguration = YamlConfiguration.loadConfiguration(userFile);
		userConfiguration.set("isTeleportOnCooldown", user.isTeleportOnCooldown());

		try {
			userConfiguration.save(userFile);
		} catch (IOException e){
			Bukkit.getLogger().severe("User '" + user.getName() + "' save failed: "+ e.getMessage());
			e.printStackTrace();
		}
	}

	private File getUserDirectory(){
		File userDirectory = new File(ChunkyPlots.getInstance().getDataFolder() + "/users");
		if(!userDirectory.exists())
			userDirectory.mkdirs();
		return userDirectory;
	}

	public User getUser(UUID uuid){
		for(User user:users)
			if(user.getPlayerUUID().equals(uuid))
				return user;
		return null;
	}
	public User getUser(OfflinePlayer player){
		return getUser(player.getUniqueId());
	}

	public static UserManager getInstance(){
		if(instance == null) {
			instance = new UserManager();
			Bukkit.getServer().getPluginManager().registerEvents(instance, ChunkyPlots.getInstance());
		}
		return instance;
	}
}
