package pl.kacpermajkowski.ChunkyPlots.protections.block.explosion;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TNTProtection implements Listener {
    Block lastTntPrimedSource = null;
    HashMap<TNTPrimed, Block> tntEntitySources = new HashMap<>();
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTntPrimed(TNTPrimeEvent event) {
        lastTntPrimedSource = event.getBlock();
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTntSpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if(!(entity instanceof TNTPrimed tnt)) return;
        tntEntitySources.put(tnt, lastTntPrimedSource);
        Bukkit.getScheduler().runTaskLater(ChunkyPlots.getInstance(), () -> {
            tntEntitySources.remove(tnt);
        }, tnt.getFuseTicks()* 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(final EntityExplodeEvent event){
        if(event.getEntity().getType() != EntityType.TNT) return;
        event.setCancelled(false);

        List<Block> blockToSave = new ArrayList<>();
        TNTPrimed tnt = (TNTPrimed) event.getEntity();
        for(Block block : event.blockList())
            if(!canTNTExplodeBlock(tnt, block))
                blockToSave.add(block);

        event.blockList().removeAll(blockToSave);
    }

    //TODO: maybe? add detection of where did redstone signal which primed  the tnt come from

    private boolean canTNTExplodeBlock(TNTPrimed tntPrimed, Block block) {
        Entity tntSource = tntPrimed.getSource();
        Plot blockPlot = PlotManager.getInstance().getPlot(block);
        if(tntSource == null) {
            Block blockSource = tntEntitySources.get(tntPrimed);
            if(blockPlot == null) return true;
            else if(blockSource != null) {
                return ProtectionUtil.canBlockAffect(blockSource, block);
            }
        } else {
            if(!tntSource.isValid()) return false;

            if (tntSource instanceof Player player)
                return ProtectionUtil.canPlayerAffect(player, blockPlot);
        }
        return false;
    }
}
