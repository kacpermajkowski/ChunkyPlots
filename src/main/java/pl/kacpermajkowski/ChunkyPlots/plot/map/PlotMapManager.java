package pl.kacpermajkowski.ChunkyPlots.plot.map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

/**
 * Service to create and open the interactive chunk map GUI for players.
 */
public class PlotMapManager {
    private final Plugin plugin;
    private final PlotManager plotManager;

    public PlotMapManager(Plugin plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
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
        World world = player.getWorld();
        if (world == null) return;

        PlotMapHolder holder = new PlotMapHolder(player, world, plotManager, chunkX, chunkZ);
        Inventory inventory = Bukkit.createInventory(holder, PlotMapHolder.INVENTORY_SIZE, TextUtil.toComponent("§8Mapa działek: §6[" + chunkX + ", " + chunkZ + "]"));
        holder.setInventory(inventory);
        holder.render();

        player.openInventory(inventory);
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }
}
