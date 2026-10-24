package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class EntityKnockbackProtection implements Listener {
    private final PlotPermissionChecker checker;

    public EntityKnockbackProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("removal")
    public void onEntityKnockback(EntityKnockbackByEntityEvent event){
        Entity affectedEntity = event.getEntity();
        Plot affectedPlot = checker.getPlot(affectedEntity.getLocation());
        if(affectedPlot == null) return;

        Entity sourceEntity = event.getSourceEntity();
        if(sourceEntity instanceof Player player){
            if(!checker.canPlayerAffect(player, affectedPlot))
                event.setCancelled(true);
        } else if (sourceEntity instanceof WindCharge windCharge){
            ProjectileSource shooter = windCharge.getShooter();
            if(shooter instanceof Player player){
                if(!checker.canPlayerAffect(player, affectedPlot))
                    event.setCancelled(true);
            } else if(shooter instanceof BlockProjectileSource bps){
                Block block = bps.getBlock();
                if(!checker.canBlockAffect(block, affectedPlot))
                    event.setCancelled(true);
            }
        }
    }
}
