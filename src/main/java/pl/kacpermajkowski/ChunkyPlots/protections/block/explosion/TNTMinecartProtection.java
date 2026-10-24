package pl.kacpermajkowski.ChunkyPlots.protections.block.explosion;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

import java.util.ArrayList;
import java.util.List;

public class TNTMinecartProtection implements Listener {
    private final PlotPermissionChecker checker;

    public TNTMinecartProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplosiveMinecartExplode(EntityExplodeEvent event) {
        if(event.getEntity().getType() != EntityType.TNT_MINECART) return;
        event.setCancelled(false);

        List<Block> blockToSave = new ArrayList<>();
        ExplosiveMinecart tntCart = (ExplosiveMinecart) event.getEntity();
        for(Block block : event.blockList())
            if(!canTNTCartExplodeBlock(tntCart, block))
                blockToSave.add(block);

        event.blockList().removeAll(blockToSave);
    }

    private boolean canTNTCartExplodeBlock(ExplosiveMinecart tntCart, Block block) {
        // ExplodingMinecart explosion source is hard to narrow so its left unfinished for now
        Plot blockPlot = checker.getPlot(block);
        return blockPlot == null;
        //TODO: Possibly allow tnt minecart to explode when it's ignited by an activation rail on a plot it's trying to explode
    }
}
