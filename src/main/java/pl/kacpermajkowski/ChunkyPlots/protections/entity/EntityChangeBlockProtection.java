package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class EntityChangeBlockProtection implements Listener {
    private final PlotPermissionChecker checker;

    public EntityChangeBlockProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        Plot plot = checker.getPlot(block);
        if (plot == null) {
            return; // Wilderness block changes are allowed
        }

        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            if (!checker.canPlayerAffect(player, plot)) {
                event.setCancelled(true);
            }
        } else {
            // Non-player entity (Wither charge, Ravager trampling crops, Enderman picking up block,
            // Zombie breaking door, Silverfish, Sheep eating grass) is prevented from altering claimed plots.
            event.setCancelled(true);
        }
    }
}
