package pl.kacpermajkowski.ChunkyPlots.plot;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class PlotBlockPlaceListener implements Listener {
    private final PlotManager plotManager;

    public PlotBlockPlaceListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event){
        if(event.isCancelled()) return;

        if(!isBlockAPlotBlock(event.getItemInHand())) return;

        boolean claimed = plotManager.claimPlot(event.getPlayer(), event.getBlockPlaced());
        if (!claimed) {
            event.setCancelled(true);
            return;
        }

        event.getBlockPlaced().setType(Material.AIR);
    }

    private boolean isBlockAPlotBlock(ItemStack block) {
        return block.isSimilar(plotManager.getPlotItem());
    }
}
