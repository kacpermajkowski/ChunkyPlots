package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class ItemFrameProtection implements Listener {
    private final PlotPermissionChecker checker;

    public ItemFrameProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    //TODO: Find other cases when item frame is allowed to be destroyed
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemFrameProtection(final HangingBreakEvent event) {
        Plot hangingPlot = checker.getPlot(event.getEntity());
        if(hangingPlot == null) return;

        if(event instanceof HangingBreakByEntityEvent entityEvent) {
            if(entityEvent.getRemover() instanceof Player player) {
                if(checker.canPlayerAffect(player, hangingPlot)) return;
            }
        }

        event.setCancelled(true);
    }
}
