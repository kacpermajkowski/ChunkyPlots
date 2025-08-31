package pl.kacpermajkowski.ChunkyPlots.protections.block.explosion;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

import java.util.ArrayList;
import java.util.List;

public class TNTProtection implements Listener {
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

    private boolean canTNTExplodeBlock(TNTPrimed tntPrimed, Block block) {
        Entity tntSource = tntPrimed.getSource();
        Plot blockPlot = PlotManager.getInstance().getPlot(block);
        if(tntSource == null) return blockPlot == null;

        if(!tntSource.isValid()) return false;

        if (tntSource instanceof Player player)
            return ProtectionUtil.canPlayerAffect(player, blockPlot);

        //TODO: Add detecting whether TNTPrimed originated from the plot its trying to explode

        return false;
    }
}
