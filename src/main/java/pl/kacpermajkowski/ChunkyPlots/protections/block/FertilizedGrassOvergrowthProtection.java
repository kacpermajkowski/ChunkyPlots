package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockFertilizeEvent;
import pl.kacpermajkowski.ChunkyPlots.listeners.EventUtils;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class FertilizedGrassOvergrowthProtection implements Listener {
    private final PlotPermissionChecker checker;

    public FertilizedGrassOvergrowthProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        Plot originPlot = checker.getPlot(event.getBlock());
        if(EventUtils.cancelEventIfPlayerNotPermitted(event, event.getPlayer(), originPlot, checker)) return;

        for(BlockState blockState : event.getBlocks()) {
            removeBlockIfOvergrowing(event.getPlayer(), blockState);
        }
    }

    public void removeBlockIfOvergrowing(Player player, BlockState blockState) {
        if(!checker.canPlayerAffect(player, blockState)) {
            blockState.setBlockData(blockState.getBlock().getBlockData());
        }
    }
}
