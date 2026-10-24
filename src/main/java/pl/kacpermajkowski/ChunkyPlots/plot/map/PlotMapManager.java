package pl.kacpermajkowski.ChunkyPlots.plot.map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

/**
 * Service to create and open the interactive chunk map GUI for players.
 */
public class PlotMapManager {
    private final Plugin plugin;
    private final PlotManager plotManager;
    private final UserManager userManager;

    public PlotMapManager(Plugin plugin, PlotManager plotManager) {
        this(plugin, plotManager, null);
    }

    public PlotMapManager(Plugin plugin, PlotManager plotManager, UserManager userManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.userManager = userManager;
    }

    /**
     * Opens the chunk map centered on the player's current chunk.
     */
    public void openMap(Player player) {
        if (player == null) return;
        int chunkX = player.getLocation().getChunk().getX();
        int chunkZ = player.getLocation().getChunk().getZ();
        openMap(player, chunkX, chunkZ);
    }

    /**
     * Opens the chunk map centered on specific chunk coordinates in player's world.
     */
    public void openMap(Player player, int chunkX, int chunkZ) {
        if (player == null) return;
        openMap(player, player.getWorld(), chunkX, chunkZ);
    }

    /**
     * Opens the chunk map centered on specific chunk coordinates in the given world.
     */
    public void openMap(Player player, World world, int chunkX, int chunkZ) {
        if (player == null) return;
        if (world == null) world = player.getWorld();
        if (world == null) return;

        PlotMapHolder holder = new PlotMapHolder(player, world, plotManager, userManager, chunkX, chunkZ);
        Inventory inventory = Bukkit.createInventory(holder, PlotMapHolder.INVENTORY_SIZE, TextUtil.toComponent("§8Mapa działek: §6[" + chunkX + ", " + chunkZ + "]"));
        holder.setInventory(inventory);
        holder.render();

        player.openInventory(inventory);
    }

    /**
     * Opens the chunk map centered directly on the given plot.
     */
    public void openMap(Player player, Plot plot) {
        if (player == null || plot == null) return;
        World targetWorld = null;
        if (Bukkit.getServer() != null && plot.getWorldName() != null) {
            targetWorld = Bukkit.getWorld(plot.getWorldName());
        }
        if (targetWorld == null) {
            targetWorld = player.getWorld();
        }
        openMap(player, targetWorld, plot.getChunkX(), plot.getChunkZ());
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }
}
