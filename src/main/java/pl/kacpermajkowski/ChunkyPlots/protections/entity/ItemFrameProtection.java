package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Bukkit;
import org.bukkit.ExplosionResult;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class ItemFrameProtection implements Listener {

    //TODO: Find other cases when item frame is allowed to be destroyed
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemFrameProtection(final HangingBreakEvent event) {
        Plot hangingPlot = PlotManager.getInstance().getPlot(event.getEntity());
        if(hangingPlot == null) return;

        if(event instanceof HangingBreakByEntityEvent entityEvent) {
            if(entityEvent.getRemover() instanceof Player player) {
                if(ProtectionUtil.canPlayerAffect(player, hangingPlot)) return;
            }
        }

        event.setCancelled(true);
    }
}
