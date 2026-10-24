package pl.kacpermajkowski.ChunkyPlots.protections.block.explosion;

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
import org.bukkit.plugin.Plugin;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TNTProtection implements Listener {
    private final PlotPermissionChecker checker;
    private final Plugin plugin;
    private final Map<TNTPrimed, Block> tntBlockSources = new ConcurrentHashMap<>();

    public TNTProtection(PlotPermissionChecker checker, Plugin plugin) {
        this.checker = checker;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTntPrimed(TNTPrimeEvent event) {
        Block tntBlock = event.getBlock();
        Plot plot = checker.getPlot(tntBlock);
        if (plot == null) return;
        if (plot.getFlag(PlotFlag.EXPLOSIONS)) return;

        Entity primingEntity = event.getPrimingEntity();
        if (primingEntity instanceof Player player) {
            if (!checker.canPlayerAffect(player, plot)) {
                event.setCancelled(true);
                return;
            }
        }

        Block primingBlock = event.getPrimingBlock();
        if (primingBlock != null) {
            if (!checker.canBlockAffect(primingBlock, plot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(final EntityExplodeEvent event){
        if (event.getEntity().getType() != EntityType.TNT) return;

        List<Block> blockToSave = new ArrayList<>();
        TNTPrimed tnt = (TNTPrimed) event.getEntity();
        for (Block block : event.blockList()) {
            if (!canTNTExplodeBlock(tnt, block)) {
                blockToSave.add(block);
            }
        }

        event.blockList().removeAll(blockToSave);
    }

    private boolean canTNTExplodeBlock(TNTPrimed tntPrimed, Block block) {
        Plot blockPlot = checker.getPlot(block);
        if (blockPlot != null && blockPlot.getFlag(PlotFlag.EXPLOSIONS)) return true;

        Entity tntSource = tntPrimed.getSource();
        if (tntSource == null) {
            Block blockSource = tntBlockSources.get(tntPrimed);
            if (blockPlot == null) return true;
            else if (blockSource != null) {
                return checker.canBlockAffect(blockSource, block);
            }
        } else {
            if (!tntSource.isValid()) return false;

            if (tntSource instanceof Player player) {
                return checker.canPlayerAffect(player, blockPlot);
            }
        }
        return false;
    }
}
