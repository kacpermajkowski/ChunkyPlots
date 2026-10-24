package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.entity.Endermite;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class EndermiteSpawnProtection implements Listener {
    private final PlotPermissionChecker checker;
    private Player lastPlayerToTeleportByPearl = null;

    public EndermiteSpawnProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

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
        if(checker.canPlayerAffect(lastPlayerToTeleportByPearl, endermite)) return;

        event.setCancelled(true);
    }
}
