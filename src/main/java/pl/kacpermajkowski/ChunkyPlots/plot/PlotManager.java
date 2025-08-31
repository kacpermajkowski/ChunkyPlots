package pl.kacpermajkowski.ChunkyPlots.plot;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.crafting.InventoryUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlotManager implements Listener {
	private static PlotManager instance;

	private final List<Plot> plots = new ArrayList<Plot>();
	private ItemStack plotItem;
	private File plotDirectory = new File(ChunkyPlots.getInstance().getDataFolder() + "/plots");

	private PlotManager(){
		loadPlots();
		setupPlotItem();
	}

	private void setupPlotItem(){
		Config config = Config.getInstance();
		HashMap<Enchantment, Integer> enchantments = new HashMap<>();
		enchantments.put(Enchantment.UNBREAKING, 1);
		plotItem = InventoryUtil.createItemStack(
				config.getPlotItemMaterial(),
				1,
				config.getPlotItemName(),
				config.getPlotItemLore(),
				enchantments,
				false);
	}

	private void loadPlots(){
		File[] files = plotDirectory.listFiles();
		if(files != null) {
			for (File file : files) {
				FileConfiguration plotData = YamlConfiguration.loadConfiguration(file);
				if (file != null) {
					String ownerUUIDstring = plotData.getString("owner.uuid");
					UUID ownerUUID = UUID.fromString(ownerUUIDstring);
					int chunkX = plotData.getInt("chunk.x");
					int chunkZ = plotData.getInt("chunk.z");
					String worldName = plotData.getString("world-name");
					List<String> whitelist = plotData.getStringList("whitelist");
					List<String> blacklist = plotData.getStringList("blacklist");;

					Plot plot = new Plot(ownerUUID, chunkX, chunkZ, worldName, UUID.fromString(file.getName()));
					for (String playerUUIDString : blacklist){
						plot.blacklistPlayer(UUID.fromString(playerUUIDString));
					}
					for (String playerUUIDString : whitelist){
						plot.whitelistPlayer(UUID.fromString(playerUUIDString));
					}

					plots.add(plot);
				}
			}
		}
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent e){
		savePlots();
	}

	public void savePlots(){
		for(Plot plot:plots) {
			savePlot(plot);
		}
	}
	public void savePlot(Plot plot) {
		try {
			File file = new File(ChunkyPlots.getInstance().getDataFolder() + "/plots/" + plot.getUUID());
			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
			fc.set("owner.uuid", plot.getOwnerUUID().toString());
			fc.set("chunk.x", plot.getChunkX());
			fc.set("chunk.z", plot.getChunkZ());
			fc.set("world-name", plot.getWorldName());
			fc.set("whitelist", plot.getWhitelist().stream().map(UUID::toString).toList());
			fc.set("blacklist", plot.getBlacklist().stream().map(UUID::toString).toList());
			fc.save(file);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public ItemStack getPlotItem() {
		return plotItem;
	}
	public List<Plot> getPlots(){
		return plots;
	}

	public Plot getPlot(Block block){
		return getPlot(block.getChunk());
	}
	public Plot getPlot(Chunk chunk){
		for(Plot plot:plots) {
			if (plot.getChunkX() == chunk.getX() && plot.getChunkZ() == chunk.getZ()) return plot;
		}
		return null;
	}
	public Plot getPlot(UUID uuid){
		for(Plot plot:plots) {
			if (plot.getUUID().equals(uuid)) return plot;
		}
		return null;
	}
	public Plot getPlot(String x, String z, String worldName){
		try {
			int parsedX = Integer.parseInt(x);
			int parsedZ = Integer.parseInt(z);
			return getPlot(parsedX, parsedZ, worldName);
		} catch (NumberFormatException e){
			return null;
		}
	}
	public Plot getPlot(int x, int z, String worldName){
		for(Plot plot:plots) {
			if (
					plot.getChunkX() == x &&
							plot.getChunkZ() == z &&
							plot.getWorldName().equals(worldName)
			) return plot;
		}
		return null;
	}
	public Plot getPlot(Location location){
		if(location == null)
			return null;

		Chunk chunk = location.getChunk();
		return getPlot(chunk);
	}

	public void disposePlot(Plot plot){
		plots.remove(plot);
		File f = new File(plotDirectory.getPath() + "/" + plot.getUUID());
		f.delete();
	}


	public void claimPlot(Player player, Block block){
		Chunk chunk = block.getChunk();
		String plotID = chunk.getX() + ";" + chunk.getZ();
		if(getPlot(chunk) != null) {
			new MessageBuilder(Message.PLOT_ALREADY_EXISTS).plotID(plotID).sendChat(player);
		}
		Plot plot = new Plot(player, chunk);
		plots.add(plot);
		savePlot(plot);
		new MessageBuilder(Message.PLOT_CREATED).plot(plot).sendChat(player);
	}

	public static PlotManager getInstance(){
		if(instance == null){
			instance = new PlotManager();
			Bukkit.getServer().getPluginManager().registerEvents(instance, ChunkyPlots.getInstance());
		}
		return instance;
	}
}
