package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.event.entity.EntityKnockbackEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class EntityKnockbackProtection implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityKnockback(EntityKnockbackByEntityEvent event){
        Entity affectedEntity = event.getEntity();
        Plot affectedPlot = PlotManager.getInstance().getPlot(affectedEntity.getLocation());
        if(affectedPlot == null) return;

        Entity sourceEntity = event.getSourceEntity();
        if(sourceEntity instanceof Player player){
            if(!PlotPermissionUtil.canPlayerAffectPlot(player, affectedPlot))
                event.setCancelled(true);
        } else if (sourceEntity instanceof WindCharge windCharge){
            ProjectileSource shooter = windCharge.getShooter();
            if(shooter instanceof Player player){
                if(!PlotPermissionUtil.canPlayerAffectPlot(player, affectedPlot))
                    event.setCancelled(true);
            } else if(shooter instanceof BlockProjectileSource bps){
                Block block = bps.getBlock();
                if(!PlotPermissionUtil.canBlockAffectPlot(block, affectedPlot))
                    event.setCancelled(true);
            }
        }
    }
}
