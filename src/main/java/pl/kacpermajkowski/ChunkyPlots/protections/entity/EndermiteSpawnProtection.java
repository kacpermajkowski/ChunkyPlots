package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class EndermiteSpawnProtection implements Listener{
    Player lastPlayerToTeleportByPearl = null;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnderpearlTeleport(PlayerTeleportEvent event) {
        if(event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;
        lastPlayerToTeleportByPearl = event.getPlayer();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if(!(event.getEntity() instanceof Endermite endermite)) return;
        if(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.ENDER_PEARL) return;
        if(lastPlayerToTeleportByPearl == null) return;
        if(ProtectionUtil.canPlayerAffect(lastPlayerToTeleportByPearl, endermite)) return;

        event.setCancelled(true);
    }
}
