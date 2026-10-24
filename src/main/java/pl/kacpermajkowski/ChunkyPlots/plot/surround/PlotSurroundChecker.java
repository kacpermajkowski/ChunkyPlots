package pl.kacpermajkowski.ChunkyPlots.plot.surround;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Validates that chunk claiming will not cause any player's plots
 * to become completely surrounded by other players within a configured search depth.
 * A player is permitted to surround their own plots.
 */
public class PlotSurroundChecker {
    private final PlotManager plotManager;
    private final Config config;

    public PlotSurroundChecker(PlotManager plotManager, Config config) {
        this.plotManager = plotManager;
        this.config = config;
    }

    public boolean isEnabled() {
        Config cfg = config != null ? config : Config.getInstance();
        return cfg == null || cfg.isSurroundProtectionEnabled();
    }

    public int getSearchDepth() {
        Config cfg = config != null ? config : Config.getInstance();
        return cfg != null ? cfg.getSurroundProtectionSearchDepth() : 5;
    }

    /**
     * Checks if claiming the specified chunk by the player would cause any player's plots
     * to become completely surrounded by other players.
     *
     * @param player The player attempting to claim
     * @param chunk  The chunk to be claimed
     * @return true if the claim would cause encirclement, false otherwise
     */
    public boolean wouldCauseEncirclement(Player player, Chunk chunk) {
        if (player == null || chunk == null) return false;
        String worldName = chunk.getWorld() != null ? chunk.getWorld().getName() : "world";
        return wouldCauseEncirclement(player.getUniqueId(), worldName, chunk.getX(), chunk.getZ());
    }

    /**
     * Checks if claiming the specified chunk coordinates by the given player UUID would cause
     * any player's plots to become completely surrounded by other players.
     *
     * @param claimingPlayerUuid UUID of the claiming player
     * @param worldName          World name
     * @param chunkX             Chunk X coordinate
     * @param chunkZ             Chunk Z coordinate
     * @return true if the claim would cause encirclement, false otherwise
     */
    public boolean wouldCauseEncirclement(UUID claimingPlayerUuid, String worldName, int chunkX, int chunkZ) {
        if (claimingPlayerUuid == null || worldName == null) return false;
        if (!isEnabled()) return false;

        int searchDepth = getSearchDepth();
        if (searchDepth <= 0) return false;

        // 1. Check if the newly claimed chunk itself would be completely surrounded by other players
        if (!canEscapeToWilderness(worldName, chunkX, chunkZ, claimingPlayerUuid, chunkX, chunkZ, claimingPlayerUuid, searchDepth)) {
            return true;
        }

        // 2. Check if this claim would cause any existing plot owned by another player to become surrounded
        Set<UUID> checkedPlots = new HashSet<>();
        for (int dx = -searchDepth; dx <= searchDepth; dx++) {
            for (int dz = -searchDepth; dz <= searchDepth; dz++) {
                int px = chunkX + dx;
                int pz = chunkZ + dz;
                Plot existing = plotManager.getPlot(px, pz, worldName);
                if (existing == null) {
                    continue;
                }
                UUID existingOwner = existing.getOwnerUUID();
                // Player can surround his own plot!
                if (existingOwner.equals(claimingPlayerUuid)) {
                    continue;
                }
                if (!checkedPlots.add(existing.getUUID())) {
                    continue;
                }

                // If existing plot cannot escape WITH this new claim...
                if (!canEscapeToWilderness(worldName, px, pz, existingOwner, chunkX, chunkZ, claimingPlayerUuid, searchDepth)) {
                    // Check if it COULD escape WITHOUT this new claim (meaning new claim is the cause)
                    if (canEscapeToWilderness(worldName, px, pz, existingOwner, chunkX, chunkZ, null, searchDepth)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines whether a plot starting at (startX, startZ) and owned by targetOwnerUuid
     * can escape to wilderness up to the specified searchDepth without crossing other players' plots.
     *
     * @param worldName              World name
     * @param startX                 Start chunk X
     * @param startZ                 Start chunk Z
     * @param targetOwnerUuid        Owner of the plot being checked
     * @param newX                   Chunk X being claimed
     * @param newZ                   Chunk Z being claimed
     * @param hypotheticalOwnerOfNew Hypothetical owner of (newX, newZ), or null if wilderness
     * @param searchDepth            Max distance to check for escape
     * @return true if an escape path to searchDepth exists, false if completely surrounded
     */
    public boolean canEscapeToWilderness(String worldName, int startX, int startZ, UUID targetOwnerUuid,
                                         int newX, int newZ, UUID hypotheticalOwnerOfNew, int searchDepth) {
        Queue<Long> queue = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();

        long startKey = PlotManager.chunkKey(startX, startZ);
        visited.add(startKey);
        queue.add(startKey);

        final int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        while (!queue.isEmpty()) {
            long currentKey = queue.poll();
            int curX = (int) currentKey;
            int curZ = (int) (currentKey >> 32);

            for (int[] dir : directions) {
                int nx = curX + dir[0];
                int nz = curZ + dir[1];
                long nKey = PlotManager.chunkKey(nx, nz);

                if (!visited.add(nKey)) {
                    continue;
                }

                UUID chunkOwner = getHypotheticalOwner(worldName, nx, nz, newX, newZ, hypotheticalOwnerOfNew);
                // Neighbor is traversable for targetOwnerUuid if wilderness or owned by targetOwnerUuid
                if (chunkOwner == null || chunkOwner.equals(targetOwnerUuid)) {
                    int dist = Math.max(Math.abs(nx - startX), Math.abs(nz - startZ));
                    if (dist >= searchDepth) {
                        return true;
                    }
                    queue.add(nKey);
                }
            }
        }

        return false;
    }

    private UUID getHypotheticalOwner(String worldName, int x, int z, int newX, int newZ, UUID hypotheticalOwnerOfNew) {
        if (x == newX && z == newZ) {
            return hypotheticalOwnerOfNew;
        }
        Plot plot = plotManager.getPlot(x, z, worldName);
        return plot != null ? plot.getOwnerUUID() : null;
    }
}
