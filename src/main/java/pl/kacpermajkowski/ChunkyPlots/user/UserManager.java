package pl.kacpermajkowski.ChunkyPlots.user;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager implements Listener {
	private static UserManager instance;

	private final Map<UUID, User> usersByUuid = new ConcurrentHashMap<>();
	private final UserRepository repository;

	public UserManager(UserRepository repository) {
		this.repository = repository;
		for (User user : repository.loadAll()) {
			usersByUuid.put(user.getPlayerUUID(), user);
		}
		instance = this;
	}

	@Deprecated
	public static UserManager getInstance() {
		return instance;
	}

	private void createUser(OfflinePlayer player) {
		final User user = new User(player);
		usersByUuid.put(user.getPlayerUUID(), user);
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
		repository.saveAll(usersByUuid.values());
	}

	public void saveUser(OfflinePlayer player) {
		if (player == null) return;
		saveUser(getUser(player));
	}

	public void saveUser(User user) {
		if (user == null) return;
		repository.save(user);
	}

	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(usersByUuid.values());
	}

	public User getUser(UUID uuid) {
		if (uuid == null) return null;
		return usersByUuid.get(uuid);
	}

	public User getUser(OfflinePlayer player) {
		if (player == null) return null;
		return getUser(player.getUniqueId());
	}
}
