package pl.kacpermajkowski.ChunkyPlots.protections.redstone;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class ShriekerProtection implements Listener {
    private final PlotPermissionChecker checker;
    private volatile Block lastBlock = null;

    public ShriekerProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBellResonate(BellResonateEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBellRing(BellRingEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBurn(BlockBurnEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDispense(BlockDispenseEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockExplode(BlockExplodeEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPistonExtend(org.bukkit.event.block.BlockPistonExtendEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPistonRetract(org.bukkit.event.block.BlockPistonRetractEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onNotePlay(NotePlayEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSculkBloom(SculkBloomEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTNTPrime(TNTPrimeEvent event) {
        registerBlockEvent(event);
    }

    private void registerBlockEvent(BlockEvent event) {
        lastBlock = event.getBlock();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVibrationSpawn(BlockReceiveGameEvent event) {
        Plot destPlot = checker.getPlot(event.getBlock());
        if (destPlot == null) return;

        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            if (checker.canPlayerAffect(player, destPlot)) return;
        } else if (entity != null) {
            Plot entityPlot = checker.getPlot(entity);
            if (entityPlot != null && entityPlot.hasTheSameOwnerAs(destPlot)) return;
        } else if (lastBlock != null) {
            if (checker.canBlockAffect(lastBlock, destPlot)) return;
        }

        event.setCancelled(true);
    }
}
