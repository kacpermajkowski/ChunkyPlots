package pl.kacpermajkowski.ChunkyPlots.protections.misc;

import com.destroystokyo.paper.event.entity.EntityZapEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class LightningProtection implements Listener {
    private final PlotPermissionChecker checker;

    public LightningProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityZap(final EntityZapEvent event) {
        LightningStrike lightning = event.getBolt();
        Plot victimPlot = checker.getPlot(event.getEntity().getLocation());
        if (victimPlot != null && victimPlot.getFlag(PlotFlag.LIGHTNING)) {
            return;
        }
        if (isProtectedFromLightning(event.getEntity(), lightning)) {
            event.setCancelled(true);
        }
    }

    private boolean isProtectedFromLightning(Entity victim, LightningStrike lightning) {
        return !checker.canEntityAffect(lightning, victim);
    }
}
