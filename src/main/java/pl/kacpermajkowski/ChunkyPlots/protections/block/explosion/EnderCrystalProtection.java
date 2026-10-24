package pl.kacpermajkowski.ChunkyPlots.protections.block.explosion;

import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.event.EventPriority.HIGHEST;

public class EnderCrystalProtection implements Listener {
    private final PlotPermissionChecker checker;

    public EnderCrystalProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = HIGHEST)
    public void onEnderCrystalExplosion(EntityExplodeEvent event) {
        if(event.getEntity().getType() != EntityType.END_CRYSTAL) return;
        event.setCancelled(false);

        List<Block> blockToSave = new ArrayList<>();
        EnderCrystal enderCrystal = (EnderCrystal) event.getEntity();
        for(Block block : event.blockList())
            if(!canEnderCrystalExplodeBlock(enderCrystal, block))
                blockToSave.add(block);

        event.blockList().removeAll(blockToSave);
    }

    private boolean canEnderCrystalExplodeBlock(EnderCrystal enderCrystal, Block block) {
        Plot blockPlot = checker.getPlot(block);
        if (blockPlot != null && blockPlot.getFlag(PlotFlag.EXPLOSIONS)) return true;
        Plot crystalPlot = checker.getPlot(enderCrystal.getLocation());
        return checker.canPlotAffectPlot(crystalPlot, blockPlot);
    }
}
