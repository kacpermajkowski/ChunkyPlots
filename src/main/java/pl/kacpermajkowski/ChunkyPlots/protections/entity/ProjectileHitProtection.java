package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class ProjectileHitProtection implements Listener {
    private final PlotPermissionChecker checker;

    public ProjectileHitProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(EntityInteractEvent event) {
        if(!(event.getEntity() instanceof Projectile projectile)) return;

        Plot affectedPlot = checker.getPlot(event.getBlock());
        if(affectedPlot == null) return;

        if(canProjectileAffectPlot(projectile, affectedPlot)) return;

        projectile.remove();
        event.setCancelled(true);
    }

//    Necessary for target block protection and arrow spam protection
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileHit(ProjectileHitEvent event) {
        Plot affectedPlot = null;

        Block hitBlock = event.getHitBlock();
        Entity hitEntity = event.getHitEntity();
        if(hitBlock != null) affectedPlot = checker.getPlot(hitBlock);
        if(hitEntity != null){
            if(hitEntity instanceof Monster monster) {
              if(monster.getRemoveWhenFarAway()) return;
            } else {
                affectedPlot = checker.getPlot(hitEntity);
            }
        }

        if(affectedPlot == null) return;

        Projectile projectile = event.getEntity();
        if(canProjectileAffectPlot(projectile, affectedPlot)) return;

        projectile.remove();
        event.setCancelled(true);
    }

    public boolean canProjectileAffectPlot(Projectile projectile, Plot affectedPlot) {
        if (projectile.getShooter() instanceof Player player) {
            return checker.canPlayerAffect(player, affectedPlot);
        } else if (projectile.getShooter() instanceof Entity entity) {
            Plot entityPlot = checker.getPlot(entity);
            return checker.canPlotAffectPlot(entityPlot, affectedPlot);
        } else return false;
    }
}
