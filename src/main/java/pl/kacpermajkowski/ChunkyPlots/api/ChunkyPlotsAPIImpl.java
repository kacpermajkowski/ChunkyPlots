package pl.kacpermajkowski.ChunkyPlots.api;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.hook.vault.VaultHook;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotBorderVisualizer;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotRelation;
import pl.kacpermajkowski.ChunkyPlots.plot.limit.PlotLimitManager;
import pl.kacpermajkowski.ChunkyPlots.plot.map.PlotMapManager;

import java.util.List;
import java.util.UUID;

public class ChunkyPlotsAPIImpl implements ChunkyPlotsAPI {
    private static ChunkyPlotsAPI instance;

    private final PlotManager plotManager;
    private final PlotLimitManager limitManager;
    private final PlotBorderVisualizer borderVisualizer;
    private final VaultHook vaultHook;
    private final PlotMapManager mapManager;

    public ChunkyPlotsAPIImpl(PlotManager plotManager, PlotLimitManager limitManager, PlotBorderVisualizer borderVisualizer, VaultHook vaultHook) {
        this(plotManager, limitManager, borderVisualizer, vaultHook, null);
    }

    public ChunkyPlotsAPIImpl(PlotManager plotManager, PlotLimitManager limitManager, PlotBorderVisualizer borderVisualizer, VaultHook vaultHook, PlotMapManager mapManager) {
        this.plotManager = plotManager;
        this.limitManager = limitManager;
        this.borderVisualizer = borderVisualizer;
        this.vaultHook = vaultHook;
        this.mapManager = mapManager;
        instance = this;
    }

    public static ChunkyPlotsAPI getInstance() {
        return instance;
    }

    public static void setInstance(ChunkyPlotsAPI api) {
        instance = api;
    }

    @Override
    public int getPlotLimit(Player player) {
        return limitManager != null ? limitManager.getPlotLimit(player) : 1;
    }

    @Override
    public int getPlotCount(Player player) {
        return limitManager != null ? limitManager.getPlotCount(player) : (player != null ? getPlotCount(player.getUniqueId()) : 0);
    }

    @Override
    public int getPlotCount(UUID playerUuid) {
        return limitManager != null ? limitManager.getPlotCount(playerUuid) : (plotManager != null ? plotManager.getPlotsByOwner(playerUuid).size() : 0);
    }

    @Override
    public int getRemainingPlots(Player player) {
        return limitManager != null ? limitManager.getRemainingPlots(player) : 0;
    }

    @Override
    public boolean hasReachedPlotLimit(Player player) {
        return limitManager != null ? limitManager.hasReachedPlotLimit(player) : false;
    }

    @Override
    public boolean canClaimPlot(Player player) {
        return limitManager != null ? limitManager.canClaimMorePlots(player) : true;
    }

    @Override
    public Plot getPlotAt(Location location) {
        return plotManager != null ? plotManager.getPlot(location) : null;
    }

    @Override
    public Plot getPlotAt(Chunk chunk) {
        return plotManager != null ? plotManager.getPlot(chunk) : null;
    }

    @Override
    public Plot getPlotAt(Player player) {
        if (player == null) return null;
        return getPlotAt(player.getLocation());
    }

    @Override
    public String getPlotOwnerName(Player player) {
        if (player == null) return "Wilderness";
        return getPlotOwnerNameAt(player.getLocation());
    }

    @Override
    public UUID getPlotOwnerUUID(Player player) {
        if (player == null) return null;
        return getPlotOwnerUUIDAt(player.getLocation());
    }

    @Override
    public String getPlotOwnerNameAt(Location location) {
        Plot plot = getPlotAt(location);
        if (plot == null) {
            return "Wilderness";
        }
        String name = plot.getOwnerName();
        if (name != null) return name;
        if (Bukkit.getServer() != null) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(plot.getOwnerUUID());
            if (op != null && op.getName() != null) return op.getName();
        }
        return "Unknown";
    }

    @Override
    public UUID getPlotOwnerUUIDAt(Location location) {
        Plot plot = getPlotAt(location);
        return plot != null ? plot.getOwnerUUID() : null;
    }

    @Override
    public boolean isWilderness(Location location) {
        return getPlotAt(location) == null;
    }

    @Override
    public boolean isWilderness(Player player) {
        return player == null || isWilderness(player.getLocation());
    }

    @Override
    public PlotRelation getPlotRelation(Player player) {
        if (player == null) return PlotRelation.WILDERNESS;
        Plot plot = getPlotAt(player);
        if (borderVisualizer != null) {
            return borderVisualizer.determineRelation(player, plot);
        }
        if (plot == null) return PlotRelation.WILDERNESS;
        if (plot.isPlayerOwner(player)) return PlotRelation.OWN_PLOT;
        if (plot.isPlayerWhitelisted(player)) return PlotRelation.MEMBER_PLOT;
        return PlotRelation.THIRD_PARTY_PLOT;
    }

    @Override
    public List<Plot> getPlots(Player player) {
        if (player == null) return List.of();
        return getPlotsByOwner(player.getUniqueId());
    }

    @Override
    public List<Plot> getPlotsByOwner(UUID ownerUuid) {
        return plotManager != null ? plotManager.getPlotsByOwner(ownerUuid) : List.of();
    }

    @Override
    public Plot getPlotById(String plotId) {
        return plotManager != null ? plotManager.getPlot(plotId) : null;
    }

    @Override
    public Plot getPlotByUuid(UUID plotUuid) {
        return plotManager != null ? plotManager.getPlot(plotUuid) : null;
    }

    @Override
    public List<Plot> getAllPlots() {
        return plotManager != null ? plotManager.getPlots() : List.of();
    }

    @Override
    public void openPlotMap(Player player) {
        if (mapManager != null && player != null) {
            mapManager.openMap(player);
        }
    }

    @Override
    public void openPlotMap(Player player, int chunkX, int chunkZ) {
        if (mapManager != null && player != null) {
            mapManager.openMap(player, chunkX, chunkZ);
        }
    }

    @Override
    public VaultHook getVaultHook() {
        return vaultHook;
    }

    @Override
    public PlotLimitManager getLimitManager() {
        return limitManager;
    }
}
