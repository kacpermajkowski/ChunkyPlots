package pl.kacpermajkowski.ChunkyPlots.plot;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.events.*;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.limit.PlotLimitManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlotManager {
	public static final int DEFAULT_MAX_PRELOAD_PLOTS = 50_000;
	public static final int MAX_WILDERNESS_CACHE_SIZE = 100_000;

	private static PlotManager instance;

	private final Map<UUID, Plot> plotsByUuid = new ConcurrentHashMap<>();
	private final Map<UUID, Set<Plot>> plotsByOwner = new ConcurrentHashMap<>();
	private final Map<String, Map<Long, Plot>> plotsByChunk = new ConcurrentHashMap<>();
	private final Map<String, Set<Long>> wildernessChunks = new ConcurrentHashMap<>();

	private final PlotRepository repository;
	private final ItemStack plotItem;
	private PlotLimitManager limitManager;

	public static long chunkKey(int chunkX, int chunkZ) {
		return ((long) chunkX & 0xFFFFFFFFL) | (((long) chunkZ & 0xFFFFFFFFL) << 32);
	}

	public PlotManager(PlotRepository repository, ItemStack plotItem) {
		this(repository, plotItem, DEFAULT_MAX_PRELOAD_PLOTS);
	}

	public PlotManager(PlotRepository repository, ItemStack plotItem, int maxPreloadPlots) {
		this.repository = repository;
		this.plotItem = plotItem;

		int totalCount = repository.count();
		if (totalCount <= maxPreloadPlots) {
			for (Plot plot : repository.loadAll()) {
				indexPlot(plot);
			}
		}
		instance = this;
	}

	private void indexPlot(Plot plot) {
		if (plot == null) return;
		plotsByUuid.put(plot.getUUID(), plot);
		plotsByOwner.computeIfAbsent(plot.getOwnerUUID(), k -> ConcurrentHashMap.newKeySet()).add(plot);
		plotsByChunk.computeIfAbsent(plot.getWorldName(), k -> new ConcurrentHashMap<>())
				.put(chunkKey(plot.getChunkX(), plot.getChunkZ()), plot);

		Set<Long> worldWilderness = wildernessChunks.get(plot.getWorldName());
		if (worldWilderness != null) {
			worldWilderness.remove(chunkKey(plot.getChunkX(), plot.getChunkZ()));
		}
	}

	private void unindexPlot(Plot plot) {
		if (plot == null) return;
		plotsByUuid.remove(plot.getUUID());
		Set<Plot> ownerPlots = plotsByOwner.get(plot.getOwnerUUID());
		if (ownerPlots != null) {
			ownerPlots.remove(plot);
		}
		Map<Long, Plot> worldPlots = plotsByChunk.get(plot.getWorldName());
		if (worldPlots != null) {
			worldPlots.remove(chunkKey(plot.getChunkX(), plot.getChunkZ()));
		}
		markWilderness(plot.getChunkX(), plot.getChunkZ(), plot.getWorldName());
	}

	private void markWilderness(int x, int z, String worldName) {
		if (worldName == null) return;
		Set<Long> worldWilderness = wildernessChunks.computeIfAbsent(worldName, k -> ConcurrentHashMap.newKeySet());
		if (worldWilderness.size() >= MAX_WILDERNESS_CACHE_SIZE) {
			worldWilderness.clear();
		}
		worldWilderness.add(chunkKey(x, z));
	}

	public void evictChunk(int x, int z, String worldName) {
		if (worldName == null) return;
		long key = chunkKey(x, z);
		Map<Long, Plot> worldPlots = plotsByChunk.get(worldName);
		if (worldPlots != null) {
			Plot plot = worldPlots.remove(key);
			if (plot != null) {
				plotsByUuid.remove(plot.getUUID());
				Set<Plot> ownerPlots = plotsByOwner.get(plot.getOwnerUUID());
				if (ownerPlots != null) {
					ownerPlots.remove(plot);
				}
			}
		}
		Set<Long> worldWilderness = wildernessChunks.get(worldName);
		if (worldWilderness != null) {
			worldWilderness.remove(key);
		}
	}

	@Deprecated
	public static PlotManager getInstance() {
		return instance;
	}

	public void setLimitManager(PlotLimitManager limitManager) {
		this.limitManager = limitManager;
	}

	public PlotLimitManager getLimitManager() {
		return limitManager;
	}

	public void savePlots() {
		for (Plot plot : plotsByUuid.values()) {
			repository.save(plot);
		}
	}

	public void savePlot(Plot plot) {
		repository.save(plot);
	}

	public ItemStack getPlotItem() {
		return plotItem;
	}

	public List<Plot> getPlots() {
		return new ArrayList<>(plotsByUuid.values());
	}

	public List<Plot> getPlotsByOwner(UUID ownerUuid) {
		if (ownerUuid == null) return List.of();
		Set<Plot> cached = plotsByOwner.get(ownerUuid);
		if (cached != null && !cached.isEmpty()) {
			return new ArrayList<>(cached);
		}
		List<Plot> dbPlots = repository.loadByOwner(ownerUuid);
		if (!dbPlots.isEmpty()) {
			for (Plot plot : dbPlots) {
				indexPlot(plot);
			}
			return dbPlots;
		}
		return List.of();
	}

	public Plot getPlot(Block block) {
		if (block == null) return null;
		return getPlot(block.getX() >> 4, block.getZ() >> 4, block.getWorld().getName());
	}

	public Plot getPlot(Chunk chunk) {
		if (chunk == null) return null;
		return getPlot(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
	}

	public Plot getPlot(UUID uuid) {
		if (uuid == null) return null;
		Plot cached = plotsByUuid.get(uuid);
		if (cached != null) return cached;

		Plot loaded = repository.loadByUuid(uuid);
		if (loaded != null) {
			indexPlot(loaded);
			return loaded;
		}
		return null;
	}

	public Plot getPlot(String idOrCoords) {
		if (idOrCoords == null || idOrCoords.isEmpty()) return null;
		// Check by UUID
		try {
			UUID uuid = UUID.fromString(idOrCoords);
			Plot plot = getPlot(uuid);
			if (plot != null) return plot;
		} catch (IllegalArgumentException ignored) {}

		// Check by ID or chunk coords (x;z or x;z;world)
		if (idOrCoords.contains(";")) {
			String[] parts = idOrCoords.split(";");
			if (parts.length == 3) {
				return getPlot(parts[0], parts[1], parts[2]);
			} else if (parts.length == 2) {
				try {
					int x = Integer.parseInt(parts[0]);
					int z = Integer.parseInt(parts[1]);
					long key = chunkKey(x, z);
					for (Map<Long, Plot> worldPlots : plotsByChunk.values()) {
						Plot found = worldPlots.get(key);
						if (found != null) return found;
					}
					// If not in cache, check current world if single world known
					if (plotsByChunk.size() == 1) {
						String singleWorld = plotsByChunk.keySet().iterator().next();
						return getPlot(x, z, singleWorld);
					}
				} catch (NumberFormatException ignored) {}
			}
		}

		// Fallback: match by Plot#getID
		for (Plot plot : plotsByUuid.values()) {
			if (plot.getID().equalsIgnoreCase(idOrCoords)) {
				return plot;
			}
		}
		return null;
	}

	public Plot getPlot(String x, String z, String worldName) {
		try {
			int parsedX = Integer.parseInt(x);
			int parsedZ = Integer.parseInt(z);
			return getPlot(parsedX, parsedZ, worldName);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Plot getPlot(int x, int z, String worldName) {
		if (worldName == null) return null;
		long key = chunkKey(x, z);

		// 1. L1 Spatial in-memory cache hit check (O(1) nanoseconds)
		Map<Long, Plot> worldPlots = plotsByChunk.get(worldName);
		if (worldPlots != null) {
			Plot cached = worldPlots.get(key);
			if (cached != null) return cached;
		}

		// 2. Wilderness negative-cache hit check (O(1) nanoseconds)
		Set<Long> worldWilderness = wildernessChunks.get(worldName);
		if (worldWilderness != null && worldWilderness.contains(key)) {
			return null;
		}

		// 3. Cache miss: on-demand query from database
		Plot loaded = repository.loadByChunk(x, z, worldName);
		if (loaded != null) {
			indexPlot(loaded);
			return loaded;
		} else {
			markWilderness(x, z, worldName);
			return null;
		}
	}

	public Plot getPlot(Location location) {
		if (location == null || location.getWorld() == null) return null;
		return getPlot(location.getBlockX() >> 4, location.getBlockZ() >> 4, location.getWorld().getName());
	}

	public Plot getPlot(Entity entity) {
		if (entity == null) return null;
		return getPlot(entity.getLocation());
	}

	public Plot getPlot(BlockState state) {
		if (state == null) return null;
		return getPlot(state.getBlock());
	}

	public boolean disposePlot(Plot plot) {
		return disposePlot(plot, null);
	}

	public boolean disposePlot(Plot plot, Player player) {
		if (plot == null) return false;

		PlotPreDisposeEvent preEvent = new PlotPreDisposeEvent(plot, player);
		Bukkit.getPluginManager().callEvent(preEvent);
		if (preEvent.isCancelled()) {
			return false;
		}

		unindexPlot(plot);
		repository.delete(plot);

		PlotDisposeEvent event = new PlotDisposeEvent(plot, player);
		Bukkit.getPluginManager().callEvent(event);
		return true;
	}

	public boolean claimPlot(Player player, Block block) {
		Chunk chunk = block.getChunk();
		String plotID = chunk.getX() + ";" + chunk.getZ();
		if (getPlot(chunk) != null) {
			new MessageBuilder(Message.PLOT_ALREADY_EXISTS).plotID(plotID).sendChat(player);
			return false;
		}

		if (limitManager != null && limitManager.hasReachedPlotLimit(player)) {
			int limit = limitManager.getPlotLimit(player);
			int current = limitManager.getPlotCount(player);
			new MessageBuilder(Message.PLOT_LIMIT_REACHED)
					.plotLimit(limit)
					.currentPlots(current)
					.sendChat(player);
			return false;
		}

		PlotPreClaimEvent preEvent = new PlotPreClaimEvent(player, chunk);
		Bukkit.getPluginManager().callEvent(preEvent);
		if (preEvent.isCancelled()) {
			return false;
		}

		Plot plot = new Plot(player, chunk);
		indexPlot(plot);
		repository.save(plot);
		new MessageBuilder(Message.PLOT_CREATED).plot(plot).sendChat(player);

		PlotClaimEvent event = new PlotClaimEvent(plot, player);
		Bukkit.getPluginManager().callEvent(event);
		return true;
	}
}
