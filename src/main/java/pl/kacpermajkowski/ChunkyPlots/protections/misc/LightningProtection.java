package pl.kacpermajkowski.ChunkyPlots.protections.misc;

import com.destroystokyo.paper.event.entity.EntityZapEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.PigZapEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class LightningProtection implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityZap(final EntityZapEvent event) {
        LightningStrike lightning = event.getBolt();
        if (isProtectedFromLightning(event.getEntity(), lightning)) {
            event.setCancelled(true);
        }
    }

    private boolean isProtectedFromLightning(Entity victim, LightningStrike lightning) {
        return !ProtectionUtil.canEntityAffect(lightning, victim);
    }
}