package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockFertilizeEvent;
import pl.kacpermajkowski.ChunkyPlots.listeners.EventUtils;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class FertilizedGrassOvergrowthProtection implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        Plot originPlot = PlotManager.getInstance().getPlot(event.getBlock());
        if(EventUtils.cancelEventIfPlayerNotPermitted(event, event.getPlayer(), originPlot)) return;

        for(BlockState blockState : event.getBlocks()) {
            removeBlockIfOvergrowing(event.getPlayer(), blockState);
        }
    }

    public static void removeBlockIfOvergrowing(Player player, BlockState blockState) {
        if(!ProtectionUtil.canPlayerAffect(player, blockState)) {
            blockState.setBlockData(blockState.getBlock().getBlockData());
        }
    }
}
