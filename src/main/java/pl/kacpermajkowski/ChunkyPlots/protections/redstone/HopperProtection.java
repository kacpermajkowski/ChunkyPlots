package pl.kacpermajkowski.ChunkyPlots.protections.redstone;

import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.InventoryHolder;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

import java.util.UUID;

public class HopperProtection implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(InventoryPickupItemEvent event) {
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if(!(inventoryHolder instanceof Hopper hopper)) return;

        Plot hopperPlot = PlotManager.getInstance().getPlot(hopper.getChunk());
        if(hopperPlot == null) return;

        UUID itemThrowerUUID = event.getItem().getThrower();
        if(hopperPlot.isPlayerWhitelisted(itemThrowerUUID)) return;
        if(hopperPlot.isPlayerOwner(itemThrowerUUID)) return;

        event.setCancelled(true);
    }
}
