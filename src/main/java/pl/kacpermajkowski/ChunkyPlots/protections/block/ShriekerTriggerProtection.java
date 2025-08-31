package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class ShriekerTriggerProtection implements Listener {
    //TODO: Add a way to disable it in config cuz its prolly a resource hog

    Block lastBlock = null;
    @EventHandler(priority = EventPriority.MONITOR)
    public void BellResonateEvent(BellResonateEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BellRingEvent(BellRingEvent event) {
        registerBlockEvent(event);
    }

//    @EventHandler(priority = EventPriority.MONITOR)
//    public void BlockBrushEvent(BlockBrushEvent event) {
//        registerBlockEvent(event);
//    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockBurnEvent(BlockBurnEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockDamageAbortEvent(BlockDamageAbortEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockDamageEvent(BlockDamageEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockDispenseEvent(BlockDispenseEvent event) {
        registerBlockEvent(event);
    }

//    @EventHandler(priority = EventPriority.MONITOR)
//    public void BlockDispenseLootEvent(BlockDispenseLootEvent event) {
//        registerBlockEvent(event);
//    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockDropItemEvent(BlockDropItemEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockExplodeEvent(BlockExplodeEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockFadeEvent(BlockFadeEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockFertilizeEvent(BlockFertilizeEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockIgniteEvent(BlockIgniteEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockPhysicsEvent(BlockPhysicsEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockPistonExtendEvent(org.bukkit.event.block.BlockPistonExtendEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockPistonRetractEvent(org.bukkit.event.block.BlockPistonRetractEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockPlaceEvent(BlockPlaceEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void BlockShearEntityEvent(BlockShearEntityEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void CauldronLevelChangeEvent(CauldronLevelChangeEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void FluidLevelChangeEvent(FluidLevelChangeEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void LeavesDecayEvent(LeavesDecayEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void NotePlayEvent(NotePlayEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void SculkBloomEvent(SculkBloomEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void SpongeAbsorbEvent(SpongeAbsorbEvent event) {
        registerBlockEvent(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void TNTPrimeEvent(TNTPrimeEvent event) {
        registerBlockEvent(event);
    }

    public void registerBlockEvent(BlockEvent event) {
        lastBlock = event.getBlock();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVibrationSpawn(BlockReceiveGameEvent event) {
        Bukkit.broadcastMessage(event.getBlock().getType().name());
        Plot destPlot = PlotManager.getInstance().getPlot(event.getBlock());
        if(destPlot == null) return;

        Bukkit.broadcastMessage("1");
        Entity entity = event.getEntity();
        if(entity instanceof Player player) {
            Bukkit.broadcastMessage("2");
            if(ProtectionUtil.canPlayerAffect(player, destPlot)) return;
        } else if (entity != null) {
            Plot entityPlot = PlotManager.getInstance().getPlot(entity);

            if(entityPlot != null && entityPlot.hasTheSameOwnerAs(destPlot)) return;
        } else if (lastBlock != null) {
            if(ProtectionUtil.canBlockAffect(lastBlock, destPlot)) return;
        }

        Bukkit.broadcastMessage("4");
        event.setCancelled(true);
    }
}
