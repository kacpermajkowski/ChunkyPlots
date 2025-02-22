package pl.kacpermajkowski.ChunkyPlots.manager;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.*;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.util.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.util.InventoryUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlotManager {
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
//		File[] files = plotDirectory.listFiles();
//		if(files != null) {
//			for (File file : files) {
//				FileConfiguration plotData = YamlConfiguration.loadConfiguration(file);
//				if (file != null) {
//					String ownerUUIDstring = plotData.getString("ownerUUID");
//					UUID ownerUUID = UUID.fromString(ownerUUIDstring);
//					int chunkX = plotData.getInt("chunkX");
//					int chunkZ = plotData.getInt("chunkZ");
//					String worldName = plotData.getString("worldName");
//					List<String> members = plotData.getStringList("members");
//					List<String> blacklist = plotData.getStringList("blacklist");;
//					HashMap<String, Boolean> flagsToConvert = new HashMap<>();
//					ConfigurationSection flagSection = plotData.getConfigurationSection("flags");
//					for(String s:flagSection.getKeys(false)){
//						flagsToConvert.put(s, flagSection.getBoolean(s));
//					}
//					HashMap<Flag, Boolean> flags = new HashMap<>();
//					for(String s:flagsToConvert.keySet()){
//						flags.put(Flag.valueOf(s),flagsToConvert.get(s));
//					}
//
//					Plot plot = new Plot(ownerUUID, chunkX, chunkZ, worldName, UUID.fromString(file.getName()));
//					for (String playerUUIDString : blacklist){
//						plot.getBlacklist().add(UUID.fromString(playerUUIDString));
//					}
//					for (String playerUUIDString : members){
//						plot.getMembers().add(UUID.fromString(playerUUIDString));
//					}
//					for (Flag flag : flags.keySet()){
//						plot.getFlags().put(flag, flags.get(flag));
//					}
//					plots.add(plot);
//				}
//			}
//		}
	}
	public void savePlots(){
		for(Plot plot:plots) {
			savePlot(plot);
		}
	}
	public void savePlot(Plot plot) {
//		try {
//			File file = new File(ChunkyPlots.getInstance().getDataFolder() + "/plots/" + plot.getUUID());
//			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
//			fc.set("ownerUUID", plot.getOwnerUUID());
//			fc.set("chunkX", plot.getChunkX());
//			fc.set("chunkZ", plot.getChunkZ());
//			fc.set("worldName", plot.getWorldName());
//			fc.set("members", plot.getMembers());
//			fc.set("blacklist", plot.getBlacklist());
//			ConfigurationSection flagSection = fc.createSection("flags");
//			for(Flag flag:plot.getFlags().keySet()){
//				flagSection.set(flag.name(), plot.getFlags().get(flag));
//			}
//			fc.save(file);
//		} catch (Exception e){
//			e.printStackTrace();
//		}
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
		Chunk chunk = location.getChunk();
		return getPlot(chunk);
	}

	public void disposePlot(Plot plot){
		plots.remove(plot);
		File f = new File(plotDirectory.getPath() + "/" + plot.getUUID());
		f.delete();
	}
	public boolean isInsidePlot(Location location){
		for(Plot plot:plots) {
			if (location.getChunk().getX() == plot.getChunkX() && location.getChunk().getZ() == plot.getChunkZ())
				return true;
		}
		return false;
	}

	public void claimPlot(Player player, Block block){
		Chunk chunk = block.getChunk();
		String plotID = chunk.getX() + ";" + chunk.getZ();
		if(getPlot(chunk) == null){
			Plot plot = new Plot(player, chunk);
			User user = UserManager.getInstance().getUser(player.getUniqueId());

			assignPlotToUserDefaultGroup(plot, user);
			plots.add(plot);
			savePlot(plot);
			new MessageBuilder(Message.PLOT_CREATED).plot(plot).send(player);
		} else new MessageBuilder(Message.PLOT_ALREADY_EXISTS).plotID(plotID).send(player);
	}

	private void assignPlotToUserDefaultGroup(Plot plot, User user){
		for(Group group:user.getGroups()){
			if(group.getName().equals("all")) group.add(plot);
		}
	}

	public static PlotManager getInstance(){
		if(instance == null){
			instance = new PlotManager();
		}
		return instance;
	}
}
