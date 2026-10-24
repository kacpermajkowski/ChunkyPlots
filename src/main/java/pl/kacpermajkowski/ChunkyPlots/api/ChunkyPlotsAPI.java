package pl.kacpermajkowski.ChunkyPlots.api;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.hook.vault.VaultHook;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotRelation;
import pl.kacpermajkowski.ChunkyPlots.plot.limit.PlotLimitManager;

import java.util.List;
import java.util.UUID;

/**
 * Public API for ChunkyPlots.
 * Allows other plugins to query player plot limits, plot ownership at locations,
 * borders, and Vault/integration data.
 */
public interface ChunkyPlotsAPI {
    static ChunkyPlotsAPI getInstance() {
        return ChunkyPlotsAPIImpl.getInstance();
    }

    static ChunkyPlotsAPI get() {
        return ChunkyPlotsAPIImpl.getInstance();
    }

    // --- Plot Limits ---

    /**
     * Resolves the maximum plot limit for a player based on permissions, Vault groups, and config.
     */
    int getPlotLimit(Player player);

    /**
     * Gets the number of plots owned by player.
     */
    int getPlotCount(Player player);

    /**
     * Gets the number of plots owned by player UUID.
     */
    int getPlotCount(UUID playerUuid);

    /**
     * Gets the remaining number of plots the player is allowed to claim.
     */
    int getRemainingPlots(Player player);

    /**
     * Checks if player has reached or exceeded their plot count limit.
     */
    boolean hasReachedPlotLimit(Player player);

    /**
     * Checks if player is allowed to claim another plot.
     */
    boolean canClaimPlot(Player player);

    // --- Location & Current Standing Plot ---

    /**
     * Gets the plot at the specified world location, or null if wilderness.
     */
    Plot getPlotAt(Location location);

    /**
     * Gets the plot at the specified chunk, or null if wilderness.
     */
    Plot getPlotAt(Chunk chunk);

    /**
     * Gets the plot the player is currently standing on, or null if in wilderness.
     */
    Plot getPlotAt(Player player);

    /**
     * Gets the owner name of the plot the player is standing on, or "Wilderness" if none.
     */
    String getPlotOwnerName(Player player);

    /**
     * Gets the owner UUID of the plot the player is standing on, or null if wilderness.
     */
    UUID getPlotOwnerUUID(Player player);

    /**
     * Gets the owner name of the plot at location, or "Wilderness" if none.
     */
    String getPlotOwnerNameAt(Location location);

    /**
     * Gets the owner UUID of the plot at location, or null if wilderness.
     */
    UUID getPlotOwnerUUIDAt(Location location);

    /**
     * Checks if location is unclaimed wilderness.
     */
    boolean isWilderness(Location location);

    /**
     * Checks if the chunk player is standing in is unclaimed wilderness.
     */
    boolean isWilderness(Player player);

    /**
     * Evaluates relationship between player and the chunk player is currently standing on.
     */
    PlotRelation getPlotRelation(Player player);

    // --- General Plot Queries ---

    /**
     * Gets all plots owned by player.
     */
    List<Plot> getPlots(Player player);

    /**
     * Gets all plots owned by owner UUID.
     */
    List<Plot> getPlotsByOwner(UUID ownerUuid);

    /**
     * Gets plot by ID string (e.g. "x;z" or "x;z;world").
     */
    Plot getPlotById(String plotId);

    /**
     * Gets plot by UUID.
     */
    Plot getPlotByUuid(UUID plotUuid);

    /**
     * Gets all loaded plots.
     */
    List<Plot> getAllPlots();

    // --- Hooks & Managers ---

    VaultHook getVaultHook();

    PlotLimitManager getLimitManager();
}
