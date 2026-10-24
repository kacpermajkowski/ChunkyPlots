package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class StructureOvergrowProtection implements Listener {
    private final PlotPermissionChecker checker;

    public StructureOvergrowProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStructureGrow(StructureGrowEvent event) {
        Plot triggerPlot = checker.getPlot(event.getLocation());

        for(BlockState blockState : event.getBlocks()) {
            Plot affectedPlot = checker.getPlot(blockState.getLocation());
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
