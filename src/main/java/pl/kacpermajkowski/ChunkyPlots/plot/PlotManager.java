package pl.kacpermajkowski.ChunkyPlots.plot;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotManager {
	private static PlotManager instance;

	private final List<Plot> plots = new ArrayList<>();
	private final PlotRepository repository;
	private final ItemStack plotItem;

	public PlotManager(PlotRepository repository, ItemStack plotItem) {
		this.repository = repository;
		this.plotItem = plotItem;
		this.plots.addAll(repository.loadAll());
		instance = this;
	}

	@Deprecated
	public static PlotManager getInstance() {
		return instance;
	}

	public void savePlots() {
		for (Plot plot : plots) {
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
		return plots;
	}

	public Plot getPlot(Block block) {
		if (block == null) return null;
		return getPlot(block.getChunk());
	}

	public Plot getPlot(Chunk chunk) {
		for (Plot plot : plots) {
			if (plot.getChunk().equals(chunk)) return plot;
		}
		return null;
	}

	public Plot getPlot(UUID uuid) {
		for (Plot plot : plots) {
			if (plot.getUUID().equals(uuid)) return plot;
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
		for (Plot plot : plots) {
			if (plot.getChunkX() == x &&
					plot.getChunkZ() == z &&
					plot.getWorldName().equals(worldName)) {
				return plot;
			}
		}
		return null;
	}

	public Plot getPlot(Location location) {
		if (location == null) return null;
		return getPlot(location.getChunk());
	}

	public Plot getPlot(Entity entity) {
		if (entity == null) return null;
		return getPlot(entity.getLocation());
	}

	public Plot getPlot(BlockState state) {
		if (state == null) return null;
		return getPlot(state.getBlock());
	}

	public void disposePlot(Plot plot) {
		plots.remove(plot);
		repository.delete(plot);
	}

	public void claimPlot(Player player, Block block) {
		Chunk chunk = block.getChunk();
		String plotID = chunk.getX() + ";" + chunk.getZ();
		if (getPlot(chunk) != null) {
			new MessageBuilder(Message.PLOT_ALREADY_EXISTS).plotID(plotID).sendChat(player);
			return;
		}
		Plot plot = new Plot(player, chunk);
		plots.add(plot);
		repository.save(plot);
		new MessageBuilder(Message.PLOT_CREATED).plot(plot).sendChat(player);
	}
}
