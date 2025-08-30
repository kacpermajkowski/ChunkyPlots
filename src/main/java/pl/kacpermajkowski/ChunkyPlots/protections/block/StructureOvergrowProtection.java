package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.InventoryUtil;

public class StructureOvergrowProtection implements Listener {
    //TODO: Verify whether event.getLocation() points to a sapling that triggered the growth
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStructureGrow(StructureGrowEvent event) {
        Plot triggerPlot = PlotManager.getInstance().getPlot(event.getLocation());

        for(BlockState blockState : event.getBlocks()) {
            Plot affectedPlot = PlotManager.getInstance().getPlot(blockState.getLocation());
            if(affectedPlot != null && (triggerPlot == null || !affectedPlot.hasTheSameOwnerAs(triggerPlot))) {
                Block sapling = event.getLocation().getBlock();
                sapling.getWorld().dropItem(event.getLocation(), new ItemStack(sapling.getType(), 1));
                sapling.setType(Material.AIR);
                event.setCancelled(true);
                return;
            }
        }
    }
}
