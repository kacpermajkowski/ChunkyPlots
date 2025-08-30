package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class EntityDamageProtection implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event){
        final Entity attacker = event.getDamager();
        final Entity victim = event.getEntity();

        if(!canEntityDamageEntity(attacker, victim)){
            event.setCancelled(true);
        }
    }

    public boolean canEntityDamageEntity(Entity attacker, Entity victim) {
        if(attacker instanceof Player player){
            return canPlayerDamageEntity(player, victim);
        } else if(attacker instanceof Projectile projectile){
            return canProjectileDamageEntity(projectile, victim);
        } else if(attacker instanceof Monster monster){
            return canMonsterDamageEntity(monster, victim);
        } else {
			if(attacker instanceof TNTPrimed tntPrimed){
				return canTntDamageEntity(tntPrimed, victim);
			}
		}
        return false;
    }

    public boolean canPlayerDamageEntity(Player player, Entity victim) {
        Plot victimPlot = PlotManager.getInstance().getPlot(victim.getLocation());
        if(victimPlot == null){
            return true;
        }

        if(victim instanceof Player){
            return true;
        } else if(victim instanceof Monster){
            return false;
        }
        return PlotPermissionUtil.canPlayerAffectPlot(player, victimPlot);
    }

    public boolean canProjectileDamageEntity(Projectile projectile, Entity victim) {
        ProjectileSource projectileSource = projectile.getShooter();
        if(projectileSource instanceof LivingEntity livingEntity){
            return canEntityDamageEntity(livingEntity, victim);
        } else if(projectileSource instanceof BlockProjectileSource blockProjectileSource){
            Plot victimPlot = PlotManager.getInstance().getPlot(victim.getLocation());
            Block block = blockProjectileSource.getBlock();
            return PlotPermissionUtil.canBlockAffectPlot(block, victimPlot);
        }
        return false;
    }

    public boolean canMonsterDamageEntity(Monster monster, Entity victim) {
        Plot victimPlot = PlotManager.getInstance().getPlot(victim.getLocation());
        return true;
        //TODO: wither?
    }

    public boolean canFireworkDamageEntity(Firework firework, Entity victim) {
        //TODO: Handle firework damage
        return false;
    }

    public boolean canTntDamageEntity(TNTPrimed tntPrimed, Entity victim) {
        //TODO: Handle tnt damage
        return false;
    }
}
