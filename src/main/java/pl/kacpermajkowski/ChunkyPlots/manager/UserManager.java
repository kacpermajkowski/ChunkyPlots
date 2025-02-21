package pl.kacpermajkowski.ChunkyPlots.manager;


import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.Group;
import pl.kacpermajkowski.ChunkyPlots.basic.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UserManager {
	private static UserManager instance;

	private List<User> users = new ArrayList<>();
	File userDirectory = new File(ChunkyPlots.getInstance().getDataFolder() + "/users");

	private UserManager(){
		loadUsers();
	}

	private void loadUsers(){
//		File[] files = userDirectory.listFiles();
//		if(files != null) {
//			for (File file : files) {
//				if (file != null) {
//					FileConfiguration userData = YamlConfiguration.loadConfiguration(file);
//					String UUIDstring = file.getName();
//					boolean isBypassingRestrictions = userData.getBoolean("isBypassingRestrictions");
//					boolean isInsidePlot = userData.getBoolean("hasEntered");
//					boolean cooldown = userData.getBoolean("cooldown");
//					boolean isTeleporting = userData.getBoolean("isTeleporting");
//					ConfigurationSection groupSection = userData.getConfigurationSection("groups");
//					Set<String> keys = groupSection.getKeys(false);
//					List<Group> groups = new ArrayList<>();
//					for (String groupName : keys) {
//						List<String> plotUUIDs = groupSection.getStringList(groupName);
//						List<UUID> plots = new ArrayList<>();
//						for(String s:plotUUIDs){
//							plots.add(UUID.fromString(s));
//						}
//						Group group = new Group(groupName);
//						group.plots = plots;
//						groups.add(group);
//					}
//
//					User user = new User(UUID.fromString(UUIDstring));
//					user.setBypassingRestrictions(isBypassingRestrictions);
//					user.setInsidePlot(isInsidePlot);
//					user.setCooldown(cooldown);
//					user.setTeleporting(isTeleporting);
//					for(Group group: groups){
//
//					}
//					users.add(user);
//				}
//			}
//		}
	}
	public void saveUsers(){
		for(User user:users) {
			saveUser(user);
		}
	}

	public void saveUser(User user) {
//		try {
//			File file = new File(ChunkyPlots.getInstance().getDataFolder() + "/users/" + user.getPlayerUUID().toString());
//			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
//			fc.set("isTeleporting", user.isTeleporting());
//			fc.set("isBypassingRestrictions", user.isBypassingRestrictions());
//			fc.set("hasEntered", user.isInsidePlot());
//			fc.set("cooldown", user.isCooldown());
//			ConfigurationSection groupSection = fc.createSection("groups");
//			for(Group group:user.getGroups()){
//				List<String> plotUUIDs = new ArrayList<>();
//				for(UUID plotUUID:group.plots) {
//					plotUUIDs.add(plotUUID.toString());
//				}
//				groupSection.set(group.getName(), plotUUIDs);
//			}
//			fc.save(file);
//		} catch (Exception e){
//			e.printStackTrace();
//		}
	}

	private List<User> obtainOnlinePlayersProfiles(List<Player> playersOnline){
		List<User> users = new ArrayList<>();
		for(Player player:playersOnline){
			User user = getUser(player.getUniqueId());
			if(user == null) users.add(new User(player.getUniqueId()));
		}
		return users;
	}

	public User getUser(UUID uuid){
		for(User user:users)
			if(user.getPlayerUUID().equals(uuid)) return user;
		return null;
	}
	public User getUser(OfflinePlayer player){
		return getUser(player.getUniqueId());
	}
	public void addUser(User user){
		users.add(user);
	}
	public void removeUser(User user){
		users.remove(user);
	}

	public List<User> getPlots(){
		return users;
	}

	public static UserManager getInstance(){
		if(instance == null)
			instance = new UserManager();
		return instance;
	}
}
