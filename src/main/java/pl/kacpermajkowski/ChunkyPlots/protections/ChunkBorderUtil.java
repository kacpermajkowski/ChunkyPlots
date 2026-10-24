package pl.kacpermajkowski.ChunkyPlots.protections;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;

import java.util.ArrayList;
import java.util.List;

public class ChunkBorderUtil {

    public static Location getSafeBorderPoint(Entity entity, Chunk chunk) {
        return getSafeBorderPoint(entity, chunk, null);
    }

    public static Location getSafeBorderPoint(Entity entity, Chunk chunk, PlotPermissionChecker checker) {
        if (entity == null) return null;
        if (chunk != null) {
            return getSafeBorderPoint(entity, chunk.getX(), chunk.getZ(), checker);
        }
        Location entityLoc = entity.getLocation();
        if (entityLoc != null) {
            return getSafeBorderPoint(entity, entityLoc.getBlockX() >> 4, entityLoc.getBlockZ() >> 4, checker);
        }
        return null;
    }

    public static Location getSafeBorderPoint(Entity entity, int centerChunkX, int centerChunkZ, PlotPermissionChecker checker) {
        if (entity == null) return null;
        Location entityLoc = entity.getLocation();
        if (entityLoc == null) return null;

        int chunkX = centerChunkX * 16;
        int chunkZ = centerChunkZ * 16;
        World world = entity.getWorld();

        // 1. Immediate border points for this chunk (64 points)
        List<double[]> borderPoints = new ArrayList<>(64);
        for (int z = chunkZ; z < chunkZ + 16; z++) {
            borderPoints.add(new double[]{chunkX - 0.5, z + 0.5}); // Left border
            borderPoints.add(new double[]{chunkX + 16 + 0.5, z + 0.5}); // Right border
        }
        for (int x = chunkX; x < chunkX + 16; x++) {
            borderPoints.add(new double[]{x + 0.5, chunkZ - 0.5}); // Top border
            borderPoints.add(new double[]{x + 0.5, chunkZ + 16 + 0.5}); // Bottom border
        }

        Location closestPoint = findClosestAllowedPoint(entity, entityLoc, borderPoints, world, checker);
        if (closestPoint != null) {
            return closestPoint;
        }

        // 2. If immediate borders are entirely blocked by adjacent denied plots, expand search in outer rings
        for (int radius = 1; radius <= 5; radius++) {
            List<double[]> ringPoints = generateRingPoints(centerChunkX, centerChunkZ, radius);
            closestPoint = findClosestAllowedPoint(entity, entityLoc, ringPoints, world, checker);
            if (closestPoint != null) {
                return closestPoint;
            }
        }

        // 3. Fallback: world spawn location if safe and allowed
        if (world != null) {
            try {
                Location spawn = world.getSpawnLocation();
                if (isLocationAllowed(entity, spawn, checker)) {
                    return findSafeLocation(spawn);
                }
            } catch (Throwable ignored) {
            }
        }

        return entityLoc;
    }

    private static List<double[]> generateRingPoints(int centerChunkX, int centerChunkZ, int radius) {
        int minChunkX = (centerChunkX - radius) * 16;
        int maxChunkX = (centerChunkX + radius + 1) * 16;
        int minChunkZ = (centerChunkZ - radius) * 16;
        int maxChunkZ = (centerChunkZ + radius + 1) * 16;

        List<double[]> points = new ArrayList<>();
        // Left and Right edges of the ring
        for (int z = minChunkZ; z < maxChunkZ; z += 2) {
            points.add(new double[]{minChunkX - 0.5, z + 0.5});
            points.add(new double[]{maxChunkX + 0.5, z + 0.5});
        }
        // Top and Bottom edges of the ring
        for (int x = minChunkX; x < maxChunkX; x += 2) {
            points.add(new double[]{x + 0.5, minChunkZ - 0.5});
            points.add(new double[]{x + 0.5, maxChunkZ + 0.5});
        }
        return points;
    }

    private static Location findClosestAllowedPoint(Entity entity, Location entityLoc, List<double[]> points, World world, PlotPermissionChecker checker) {
        if (entityLoc == null) return null;
        double minDistance = Double.MAX_VALUE;
        Location closestPoint = null;

        for (double[] point : points) {
            Location borderLoc = new Location(world, point[0], entityLoc.getY(), point[1]);

            if (!isLocationAllowed(entity, borderLoc, checker)) {
                continue;
            }

            double distance = entityLoc.distance(borderLoc);
            if (distance < minDistance) {
                Location safeLoc = findSafeLocation(borderLoc);
                if (isLocationAllowed(entity, safeLoc, checker)) {
                    minDistance = distance;
                    closestPoint = safeLoc;
                }
            }
        }
        return closestPoint;
    }

    public static boolean isLocationAllowed(Entity entity, Location loc, PlotPermissionChecker checker) {
        if (checker == null || loc == null) return true;
        Plot plot = checker.getPlot(loc);
        if (plot == null) return true;

        if (entity instanceof Player player) {
            if (plot.isPlayerBlacklisted(player) && !checker.canPlayerAffect(player, plot)) {
                return false;
            }
        } else if (entity != null) {
            for (Entity passenger : entity.getPassengers()) {
                if (passenger instanceof Player player) {
                    if (plot.isPlayerBlacklisted(player) && !checker.canPlayerAffect(player, plot)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static Location findSafeLocation(Location loc) {
        if (loc == null) return null;
        World world = loc.getWorld();
        if (world == null) return loc;

        // Search within a 4-block vertical range for a safe position
        for (int yOffset = 0; yOffset <= 4; yOffset++) {
            if (yOffset == 0) {
                if (isSafe(loc)) return loc;
            } else {
                Location down = loc.clone().add(0, -yOffset, 0);
                if (isSafe(down)) return down;
                Location up = loc.clone().add(0, yOffset, 0);
                if (isSafe(up)) return up;
            }
        }

        return loc;
    }

    private static boolean isSafe(Location loc) {
        if (loc == null) return false;
        World world = loc.getWorld();
        if (world == null) return true;
        try {
            Block ground = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
            Block feet = world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            Block head = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());

            return ground.getType().isSolid() && feet.getType() == Material.AIR && head.getType() == Material.AIR;
        } catch (Throwable e) {
            return true;
        }
    }
}
