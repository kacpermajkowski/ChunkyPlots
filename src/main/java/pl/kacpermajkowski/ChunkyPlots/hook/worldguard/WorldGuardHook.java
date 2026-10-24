package pl.kacpermajkowski.ChunkyPlots.hook.worldguard;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Service hook into WorldGuard API.
 * Safely checks region overlap if WorldGuard is present on the server.
 */
public class WorldGuardHook {
    public interface RegionOverlapChecker {
        boolean hasOverlappingRegion(World world, int chunkX, int chunkZ);
    }

    private final Plugin plugin;
    private final Logger logger;
    private boolean hooked = false;
    private RegionOverlapChecker overlapChecker;

    public WorldGuardHook(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin != null ? plugin.getLogger() : Logger.getLogger("ChunkyPlots");
        initialize();
    }

    public void initialize() {
        if (Bukkit.getPluginManager() == null || Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            if (logger != null) {
                logger.info("WorldGuard not found. WorldGuard region protection disabled.");
            }
            this.hooked = false;
            return;
        }

        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            this.hooked = true;
            if (logger != null) {
                logger.info("Successfully hooked into WorldGuard.");
            }
        } catch (Throwable t) {
            if (logger != null) {
                logger.warning("Could not initialize WorldGuard hook: " + t.getMessage());
            }
            this.hooked = false;
        }
    }

    public boolean isHooked() {
        return hooked;
    }

    public void setHooked(boolean hooked) {
        this.hooked = hooked;
    }

    public void setOverlapChecker(RegionOverlapChecker overlapChecker) {
        this.overlapChecker = overlapChecker;
        if (overlapChecker != null) {
            this.hooked = true;
        }
    }

    public RegionOverlapChecker getOverlapChecker() {
        return overlapChecker;
    }

    /**
     * Checks if the given chunk overlaps with any WorldGuard region (excluding __global__).
     *
     * @param chunk The Bukkit chunk
     * @return true if the chunk overlaps an existing WorldGuard region, false otherwise
     */
    public boolean hasOverlappingRegion(Chunk chunk) {
        if (chunk == null) return false;
        return hasOverlappingRegion(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    /**
     * Checks if the chunk at the given world and chunk coordinates overlaps with any WorldGuard region (excluding __global__).
     *
     * @param world  The Bukkit world
     * @param chunkX The chunk X coordinate
     * @param chunkZ The chunk Z coordinate
     * @return true if the chunk overlaps an existing WorldGuard region, false otherwise
     */
    public boolean hasOverlappingRegion(World world, int chunkX, int chunkZ) {
        if (!hooked || world == null) {
            return false;
        }

        if (overlapChecker != null) {
            return overlapChecker.hasOverlappingRegion(world, chunkX, chunkZ);
        }

        try {
            return WorldGuardPlatformHandler.hasOverlappingRegion(world, chunkX, chunkZ);
        } catch (Throwable t) {
            if (logger != null) {
                logger.warning("Error querying WorldGuard for overlapping regions: " + t.getMessage());
            }
            return false;
        }
    }
}
