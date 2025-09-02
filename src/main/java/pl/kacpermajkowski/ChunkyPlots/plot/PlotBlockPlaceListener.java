package pl.kacpermajkowski.ChunkyPlots.plot;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class PlotBlockPlaceListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event){
        if(event.isCancelled()) return;
        
        if(!isBlockAPlotBlock(event.getItemInHand())) return;
        if(!canPlotBeCreated(event)) {
            event.setCancelled(true);
            return;
        }

        PlotManager.getInstance().claimPlot(event.getPlayer(), event.getBlockPlaced());
        event.getBlockPlaced().setType(Material.AIR);
    }

    private boolean canPlotBeCreated(BlockPlaceEvent event) {
        return PlotManager.getInstance().getPlot(event.getBlockPlaced()) == null;
    }
    private boolean isBlockAPlotBlock(ItemStack block) {
        return block.isSimilar(PlotManager.getInstance().getPlotItem());
    }
}
