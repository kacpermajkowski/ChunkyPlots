package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class PlayerLeashEntityProtection implements Listener {
    private final PlotPermissionChecker checker;

    public PlayerLeashEntityProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeashEntity(final PlayerLeashEntityEvent event){
        final Location entityLocation = event.getEntity().getLocation();
        final Player player = event.getPlayer();
        final Plot eventPlot = checker.getPlot(entityLocation);

        if (eventPlot != null && eventPlot.getFlag(PlotFlag.ANIMAL_LEASH)) return;

        if (!checker.canPlayerAffect(player, eventPlot)) {
            event.setCancelled(true);
            ProtectionNotifier.notify(player, Message.PROTECTION_ENTITY_LEASH, eventPlot);
        }
    }
}
