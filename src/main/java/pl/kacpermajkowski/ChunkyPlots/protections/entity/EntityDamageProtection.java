package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class EntityDamageProtection implements Listener {
    private final PlotPermissionChecker checker;

    public EntityDamageProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event){
        final Entity attacker = event.getDamager();
        final Entity victim = event.getEntity();

        if(canEntityDamageEntity(attacker, victim)) return;
        event.setCancelled(true);

        if (attacker instanceof Player player) {
            Plot victimPlot = checker.getPlot(victim);
            ProtectionNotifier.notify(player, Message.PROTECTION_ENTITY_DAMAGE, victimPlot);
        } else if (attacker instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            Plot victimPlot = checker.getPlot(victim);
            ProtectionNotifier.notify(player, Message.PROTECTION_ENTITY_DAMAGE, victimPlot);
        }
    }

    public boolean canEntityDamageEntity(Entity attacker, Entity victim) {
        if(attacker instanceof Player player){
            return canPlayerDamageEntity(player, victim);
        } else if(attacker instanceof Projectile projectile){
            return canProjectileDamageEntity(projectile, victim);
        } else if(attacker instanceof TNTPrimed tntPrimed){
            return canTntDamageEntity(tntPrimed, victim);
        } else if(attacker instanceof LightningStrike lightning){
            return canLightningDamageEntity(lightning, victim);
        }

        return checker.canEntityAffect(attacker, victim);
    }

    public boolean canPlayerDamageEntity(Player player, Entity victim) {
        if(victim instanceof Player){
            return true;
        }
        if(victim instanceof Monster monster && monster.getRemoveWhenFarAway()){
            return true;
        }
        return checker.canPlayerAffect(player, victim);
    }

    public boolean canProjectileDamageEntity(Projectile projectile, Entity victim) {
        ProjectileSource projectileSource = projectile.getShooter();
        if(projectileSource instanceof LivingEntity livingEntity){
            return canEntityDamageEntity(livingEntity, victim);
        } else if(projectileSource instanceof BlockProjectileSource blockProjectileSource){
            Block block = blockProjectileSource.getBlock();
            return checker.canBlockAffectEntity(block, victim);
        }

        return false;
    }

    public boolean canTntDamageEntity(TNTPrimed tntPrimed, Entity victim) {
        Entity source = tntPrimed.getSource();
        if(source != null){
            return canEntityDamageEntity(source, victim);
        }
        return checker.canEntityAffect(tntPrimed, victim);
    }

    public boolean canLightningDamageEntity(LightningStrike lightning, Entity victim) {
        Entity causingEntity = lightning.getCausingEntity();
        if(causingEntity != null){
            return canEntityDamageEntity(causingEntity, victim);
        }

        return checker.canEntityAffect(lightning, victim);
    }
}
