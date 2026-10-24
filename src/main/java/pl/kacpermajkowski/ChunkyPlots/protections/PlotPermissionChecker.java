package pl.kacpermajkowski.ChunkyPlots.protections;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotPermissionChecker {
    private final PlotManager plotManager;
    private final UserManager userManager;

    public PlotPermissionChecker(PlotManager plotManager, UserManager userManager) {
        this.plotManager = plotManager;
        this.userManager = userManager;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public boolean isYWithinBounds(int y) {
        return plotManager != null && plotManager.isYWithinPlotBounds(y);
    }

    public Plot getPlot(Block block) {
        return plotManager.getPlot(block);
    }

    public Plot getPlot(Chunk chunk) {
        return plotManager.getPlot(chunk);
    }

    public Plot getPlot(Location location) {
        return plotManager.getPlot(location);
    }

    public Plot getPlot(Entity entity) {
        return plotManager.getPlot(entity);
    }

    public Plot getPlot(BlockState state) {
        return plotManager.getPlot(state);
    }

    public boolean canPlayerAffect(Player player, Plot plot) {
        if (plot == null) return true;
        if (player == null) return false;

        if (plot.isPlayerOwner(player) || plot.isPlayerWhitelisted(player)) return true;
        User user = userManager.getUser(player);
        return user != null && user.isBypassingRestrictions();
    }

    public boolean canPlayerAffect(Player player, Block block) {
        Plot plot = plotManager.getPlot(block);
        return canPlayerAffect(player, plot);
    }

    public boolean canPlayerAffect(Player player, BlockState state) {
        Plot plot = plotManager.getPlot(state);
        return canPlayerAffect(player, plot);
    }

    public boolean canPlayerAffect(Player player, Entity entity) {
        Plot plot = plotManager.getPlot(entity);
        return canPlayerAffect(player, plot);
    }

    public boolean canPlayerAffectPlots(Player player, List<Plot> plots) {
        if (plots == null || plots.isEmpty()) return true;
        if (player == null) return false;

        for (Plot affectedPlot : plots) {
            if (!canPlayerAffect(player, affectedPlot)) return false;
        }
        return true;
    }

    public boolean canPlayerAffectBlocks(Player player, List<Block> blocks) {
        if (blocks == null || blocks.isEmpty()) return true;
        if (player == null) return false;

        List<Plot> plots = new ArrayList<>();
        for (Block block : blocks) {
            plots.add(plotManager.getPlot(block));
        }
        return canPlayerAffectPlots(player, plots);
    }

    public boolean canPlayerAffectEntities(Player player, List<Entity> entities) {
        if (entities == null || entities.isEmpty()) return true;
        if (player == null) return false;

        List<Plot> plots = new ArrayList<>();
        for (Entity entity : entities) {
            plots.add(plotManager.getPlot(entity));
        }
        return canPlayerAffectPlots(player, plots);
    }

    public boolean canOwnerAffect(UUID ownerUuid, Plot plot) {
        if (plot == null) return true;
        if (ownerUuid == null) return false;
        return plot.isPlayerOwner(ownerUuid) || plot.isPlayerWhitelisted(ownerUuid);
    }

    public boolean canOwnerAffectPlots(UUID ownerUuid, List<Plot> plots) {
        if (plots == null || plots.isEmpty()) return true;
        for (Plot p : plots) {
            if (!canOwnerAffect(ownerUuid, p)) return false;
        }
        return true;
    }

    public boolean canBlockAffect(Block block, Plot affectedPlot) {
        Plot blockPlot = plotManager.getPlot(block);
        return canPlotAffectPlot(blockPlot, affectedPlot);
    }

    public boolean canBlockAffect(Block block, Block affectedBlock) {
        Plot affectedPlot = plotManager.getPlot(affectedBlock);
        return canBlockAffect(block, affectedPlot);
    }

    public boolean canBlockAffect(Block block, BlockState affectedBlock) {
        Plot plot = plotManager.getPlot(affectedBlock);
        return canBlockAffect(block, plot);
    }

    public boolean canBlockAffectEntity(Block block, Entity entity) {
        Plot blockPlot = plotManager.getPlot(block);
        Plot entityPlot = plotManager.getPlot(entity);
        return canPlotAffectPlot(blockPlot, entityPlot);
    }

    public boolean canEntityAffect(Entity entity, Entity affectedEntity) {
        Plot entityPlot = plotManager.getPlot(entity);
        Plot affectedPlot = plotManager.getPlot(affectedEntity);
        return canPlotAffectPlot(entityPlot, affectedPlot);
    }

    public boolean canPlotAffectPlot(Plot source, Plot destination) {
        if (destination == null) return true;
        if (source == null) return false;
        if (source.hasTheSameOwnerAs(destination)) return true;
        if (destination.isPlayerWhitelisted(source.getOwnerUUID())) return true;

        User ownerUser = userManager.getUser(destination.getOwnerUUID());
        return ownerUser != null && ownerUser.isBypassingRestrictions();
    }

    public boolean canPlotAffectPlots(Plot plot, List<Plot> plots) {
        if (plots == null || plots.isEmpty()) return true;
        if (plot == null) return false;

        for (Plot affectedPlot : plots) {
            if (!canPlotAffectPlot(plot, affectedPlot)) return false;
        }
        return true;
    }
}
