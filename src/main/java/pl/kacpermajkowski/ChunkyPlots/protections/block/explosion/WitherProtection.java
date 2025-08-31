package pl.kacpermajkowski.ChunkyPlots.protections.block.explosion;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WitherProtection implements Listener {
    private final HashMap<UUID, Player> witherSummoners = new HashMap<>();
    private Player lastWitherBlockPlacer;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntitySpawn(final EntitySpawnEvent event){
        if(event.getEntity() instanceof Wither){
            witherSummoners.put(event.getEntity().getUniqueId(), lastWitherBlockPlacer);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockPlaceEvent event){
        Material blockMaterial = event.getBlockPlaced().getType();
        if(blockMaterial.equals(Material.SOUL_SAND) || blockMaterial.equals(Material.WITHER_SKELETON_SKULL)){
            lastWitherBlockPlacer = event.getPlayer();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(final EntityExplodeEvent event){
        Entity exploder = event.getEntity();
        Wither wither = null;
        if(exploder instanceof WitherSkull witherSkull)
            if(witherSkull.getShooter() instanceof Wither w) wither = w;
        else if (exploder instanceof Wither w) wither = w;
        if(wither == null) return;

        event.setCancelled(false);

        List<Block> blocksToSave = new ArrayList<>();
        for(Block block : event.blockList())
            if(canWitherExplodeBlock(wither, block))
                blocksToSave.add(block);

        event.blockList().removeAll(blocksToSave);

    }

    private boolean canWitherExplodeBlock(Wither wither, Block block) {
        Plot blockPlot = PlotManager.getInstance().getPlot(block);
        UUID witherUUID = wither.getUniqueId();
        Player summoner = witherSummoners.get(witherUUID);

        if(summoner == null) return false;
        else return ProtectionUtil.canPlayerAffect(summoner, blockPlot);
    }

    private boolean canWitherSkullExplodeBlock(WitherSkull witherSkull, Block block) {
        if(!(witherSkull.getShooter() instanceof Wither wither)) return false;
        else return canWitherExplodeBlock(wither, block);
    }
}
