package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class EntityDamageProtection implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event){
        final Entity attacker = event.getDamager();
        final Entity victim = event.getEntity();

        if(canEntityDamageEntity(attacker, victim)) return;
        event.setCancelled(true);
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

        return ProtectionUtil.canEntityAffect(attacker, victim);
    }

    public boolean canPlayerDamageEntity(Player player, Entity victim) {
        if(victim instanceof Player){
            return true;
        }
        if(victim instanceof Monster monster && monster.getRemoveWhenFarAway()){
            return true;
        }
        return ProtectionUtil.canPlayerAffect(player, victim);
    }

    public boolean canProjectileDamageEntity(Projectile projectile, Entity victim) {
        ProjectileSource projectileSource = projectile.getShooter();
        if(projectileSource instanceof LivingEntity livingEntity){
            return canEntityDamageEntity(livingEntity, victim);
        } else if(projectileSource instanceof BlockProjectileSource blockProjectileSource){
            Block block = blockProjectileSource.getBlock();
            return ProtectionUtil.canBlockAffectEntity(block, victim);
        }

        return false;
    }


    public boolean canTntDamageEntity(TNTPrimed tntPrimed, Entity victim) {
        Entity source = tntPrimed.getSource();
        if(source != null){
            return canEntityDamageEntity(source, victim);
        }
        return ProtectionUtil.canEntityAffect(tntPrimed, victim);
    }

    public boolean canLightningDamageEntity(LightningStrike lightning, Entity victim) {
        Entity causingEntity = lightning.getCausingEntity();
        if(causingEntity != null){
            return canEntityDamageEntity(causingEntity, victim);
        }

        return ProtectionUtil.canEntityAffect(lightning, victim);
    }
}