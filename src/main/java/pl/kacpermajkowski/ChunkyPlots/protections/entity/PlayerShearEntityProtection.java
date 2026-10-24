package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class PlayerShearEntityProtection implements Listener {
    private final PlotPermissionChecker checker;

    public PlayerShearEntityProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerShearEntity(final PlayerShearEntityEvent event){
        final Location entityLocation = event.getEntity().getLocation();
        final Player player = event.getPlayer();
        final Plot eventPlot = checker.getPlot(entityLocation);

        if (!checker.canPlayerAffect(player, eventPlot)) {
            event.setCancelled(true);
            ProtectionNotifier.notify(player, Message.PROTECTION_ENTITY_SHEAR, eventPlot);
        }
    }
}
