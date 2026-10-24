package pl.kacpermajkowski.ChunkyPlots.user;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager implements Listener {
	private static UserManager instance;

	private final Map<UUID, User> usersByUuid = new ConcurrentHashMap<>();
	private final UserRepository repository;
	private PlotManager plotManager;

	public UserManager(UserRepository repository) {
		this(repository, null);
	}

	public UserManager(UserRepository repository, PlotManager plotManager) {
		this.repository = repository;
		this.plotManager = plotManager;
		if (repository instanceof SqliteUserRepository sqliteRepo && plotManager != null) {
			sqliteRepo.setPlotResolver(plotManager::getPlot);
		}
		for (User user : repository.loadAll()) {
			if (plotManager != null) {
				user.bindPlotSupplier(() -> plotManager.getPlotsByOwner(user.getPlayerUUID()));
			}
			usersByUuid.put(user.getPlayerUUID(), user);
		}
		instance = this;
	}

	public void setPlotManager(PlotManager plotManager) {
		this.plotManager = plotManager;
		if (repository instanceof SqliteUserRepository sqliteRepo && plotManager != null) {
			sqliteRepo.setPlotResolver(plotManager::getPlot);
		}
		for (User user : usersByUuid.values()) {
			if (plotManager != null) {
				user.bindPlotSupplier(() -> plotManager.getPlotsByOwner(user.getPlayerUUID()));
			}
		}
	}

	@Deprecated
	public static UserManager getInstance() {
		return instance;
	}

	private User createUser(OfflinePlayer player) {
		final User user = new User(player);
		if (plotManager != null) {
			user.bindPlotSupplier(() -> plotManager.getPlotsByOwner(user.getPlayerUUID()));
		}
		usersByUuid.put(user.getPlayerUUID(), user);
		saveUser(user);
		return user;
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
		User user = usersByUuid.get(uuid);
		if (user == null && BukkitTestOrLiveCheck(uuid)) {
			user = new User(uuid);
			if (plotManager != null) {
				user.bindPlotSupplier(() -> plotManager.getPlotsByOwner(uuid));
			}
			usersByUuid.put(uuid, user);
		}
		return user;
	}

	public User getUser(OfflinePlayer player) {
		if (player == null) return null;
		User user = getUser(player.getUniqueId());
		if (user == null) {
			user = createUser(player);
		}
		return user;
	}

	private boolean BukkitTestOrLiveCheck(UUID uuid) {
		return uuid != null;
	}
}
