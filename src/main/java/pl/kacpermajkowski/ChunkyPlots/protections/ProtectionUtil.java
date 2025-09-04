package pl.kacpermajkowski.ChunkyPlots.protections;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

import java.util.ArrayList;
import java.util.List;

public abstract class ProtectionUtil {
    public static boolean canPlayerAffect(Player player, Plot plot){
        if (player == null) return false;
        if (plot == null) return true;

        else return plot.isPlayerOwner(player) || plot.isPlayerWhitelisted(player);
    }

    public static boolean canPlayerAffect(Player player, Block block){
        Plot plot = PlotManager.getInstance().getPlot(block);
        return canPlayerAffect(player, plot);
    }

    public static boolean canPlayerAffect(Player player, BlockState state){
        Plot plot = PlotManager.getInstance().getPlot(state);
        return canPlayerAffect(player, plot);
    }

    public static boolean canPlayerAffect(Player player, Entity entity){
        Plot plot = PlotManager.getInstance().getPlot(entity);
        return canPlayerAffect(player, plot);
    }

    public static boolean canPlayerAffectPlots(Player player, List<Plot> plots){
        if(player == null) return false;
        if(plots == null || plots.isEmpty()) return true;

        for(Plot affectedPlot:plots){
            if(!canPlayerAffect(player, affectedPlot)) return false;
        }
        return true;
    }

    public static boolean canPlayerAffectBlocks(Player player, List<Block> blocks){
        if(player == null) return false;
        if(blocks == null || blocks.isEmpty()) return true;

        List<Plot> plots = new ArrayList<>();
        for(Block block : blocks){
            plots.add(PlotManager.getInstance().getPlot(block));
        }
        return canPlayerAffectPlots(player, plots);
    }

    public static boolean canPlayerAffectEntities(Player player, List<Entity> entities){
        if(player == null) return false;
        if(entities == null || entities.isEmpty()) return true;

        List<Plot> plots = new ArrayList<>();
        for(Entity entity : entities){
            plots.add(PlotManager.getInstance().getPlot(entity));
        }
        return canPlayerAffectPlots(player, plots);
    }

    public static boolean canBlockAffect(Block block, Plot affectedPlot){
        Plot blockPlot = PlotManager.getInstance().getPlot(block);
        return canPlotAffectPlot(blockPlot, affectedPlot);
    }

    public static boolean canBlockAffect(Block block, Block affectedBlock){
        Plot affectedPlot = PlotManager.getInstance().getPlot(affectedBlock);
        return canBlockAffect(block, affectedPlot);
    }

    public static boolean canBlockAffect(Block block, BlockState affectedBlock){
        Plot plot = PlotManager.getInstance().getPlot(affectedBlock);
        return canBlockAffect(block, plot);
    }

    public static boolean canBlockAffectEntity(Block block, Entity entity){
        Plot blockPlot = PlotManager.getInstance().getPlot(block);
        Plot entityPlot = PlotManager.getInstance().getPlot(entity);
        return canPlotAffectPlot(blockPlot, entityPlot);
    }

    public static boolean canEntityAffect(Entity entity, Entity affectedEntity){
        Plot entityPlot = PlotManager.getInstance().getPlot(entity);
        Plot affectedPlot = PlotManager.getInstance().getPlot(affectedEntity);
        return canPlotAffectPlot(entityPlot, affectedPlot);
    }

    public static boolean canPlotAffectPlot(Plot plot, Plot affectedPlot){
        if(affectedPlot == null) return true;
        else if(plot == null) return false;

        return plot.hasTheSameOwnerAs(affectedPlot);
    }

    public static boolean canPlotAffectPlots(Plot plot, List<Plot> plots){
        if(plots == null || plots.isEmpty()) return true;
        else if(plot == null) return false;

        for(Plot affectedPlot:plots){
            if(!canPlotAffectPlot(plot,affectedPlot)) return false;
        }
        return true;
    }

    //Written by chatgpt - it needs optimalization probably, but i don't think it's called very often
    public static Location getSafeBorderPoint(Player player, Chunk chunk) {
        int chunkX = chunk.getX() * 16;
        int chunkZ = chunk.getZ() * 16;
        World world = player.getWorld();

        Location playerLoc = player.getLocation();
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
        return findSafeLocation(closestPoint, player);
    }

    private static Location findSafeLocation(Location loc, Player player) {
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
