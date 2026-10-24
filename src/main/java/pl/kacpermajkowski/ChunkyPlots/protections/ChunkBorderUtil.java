package pl.kacpermajkowski.ChunkyPlots.protections;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class ChunkBorderUtil {

    // Written by chatgpt - it needs optimalization probably, but i don't think it's called very often
    public static Location getSafeBorderPoint(Entity entity, Chunk chunk) {
        int chunkX = chunk.getX() * 16;
        int chunkZ = chunk.getZ() * 16;
        World world = entity.getWorld();

        Location playerLoc = entity.getLocation();
        double minDistance = Double.MAX_VALUE;
        Location closestPoint = null;

        // Define continuous border locations with 0.5 offset
        double[][] borderPoints = new double[64][2];
        int index = 0;

        // Left and Right borders (moving in Z direction)
        for (int z = chunkZ; z < chunkZ + 16; z++) {
            borderPoints[index++] = new double[]{chunkX - 0.5, z + 0.5}; // Left border
            borderPoints[index++] = new double[]{chunkX + 16 + 0.5, z + 0.5}; // Right border
        }

        // Top and Bottom borders (moving in X direction)
        for (int x = chunkX; x < chunkX + 16; x++) {
            borderPoints[index++] = new double[]{x + 0.5, chunkZ - 0.5}; // Top border
            borderPoints[index++] = new double[]{x + 0.5, chunkZ + 16 + 0.5}; // Bottom border
        }

        // Find the closest border point
        for (double[] point : borderPoints) {
            Location borderLoc = new Location(world, point[0], playerLoc.getY(), point[1]);

            double distance = playerLoc.distance(borderLoc);
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = borderLoc;
            }
        }

        // Ensure the location is safe
        return findSafeLocation(closestPoint);
    }

    private static Location findSafeLocation(Location loc) {
        World world = loc.getWorld();

        // Search within a 2-block range for a safe position
        for (int yOffset = -2; yOffset <= 2; yOffset++) {
            Location checkLoc = loc.clone().add(0, yOffset, 0);

            if (isSafe(checkLoc)) {
                return checkLoc;
            }
        }

        // No safe position found, return the original (player might suffocate)
        return loc;
    }

    private static boolean isSafe(Location loc) {
        World world = loc.getWorld();
        Block ground = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        Block feet = world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        Block head = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());

        return ground.getType().isSolid() && feet.getType() == Material.AIR && head.getType() == Material.AIR;
    }
}
