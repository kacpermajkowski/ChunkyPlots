package pl.kacpermajkowski.ChunkyPlots.user;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class UserManager implements Listener {
	private static UserManager instance;

	private final Set<User> users = new HashSet<>();
	private final UserRepository repository;

	public UserManager(UserRepository repository) {
		this.repository = repository;
		this.users.addAll(repository.loadAll());
		instance = this;
	}

	@Deprecated
	public static UserManager getInstance() {
		return instance;
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
		repository.saveAll(users);
	}

	public void saveUser(OfflinePlayer player) {
		saveUser(getUser(player));
	}

	public void saveUser(User user) {
		if (user == null) return;
		repository.save(user);
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
