package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class ItemFrameProtection implements Listener {
    private final PlotPermissionChecker checker;

    public ItemFrameProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemFrameProtection(final HangingBreakEvent event) {
        Plot hangingPlot = checker.getPlot(event.getEntity());
        if (hangingPlot == null) return;
        if (hangingPlot.getFlag(PlotFlag.ITEM_FRAME)) return;

        if (event.getCause() == HangingBreakEvent.RemoveCause.PHYSICS) {
            // Physics pops (e.g. supporting block broken by authorized player)
            return;
        }

        Player breakerPlayer = null;
        if (event instanceof HangingBreakByEntityEvent entityEvent) {
            Entity remover = entityEvent.getRemover();
            if (remover instanceof Player player) {
                if (checker.canPlayerAffect(player, hangingPlot)) return;
                breakerPlayer = player;
            } else if (remover instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
                if (checker.canPlayerAffect(player, hangingPlot)) return;
                breakerPlayer = player;
            }
        }

        event.setCancelled(true);
        if (breakerPlayer != null) {
            ProtectionNotifier.notify(breakerPlayer, Message.PROTECTION_HANGING_BREAK, hangingPlot);
        }
    }
}
