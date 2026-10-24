package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class VehicleDamageListener implements Listener {
    private final PlotPermissionChecker checker;

    public VehicleDamageListener(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleDamage(final VehicleDamageEvent event){
        final Entity attacker = event.getAttacker();
        final Entity vehicle = event.getVehicle();

        Plot vehiclePlot = checker.getPlot(vehicle);

        if(attacker instanceof Player player) {
            if (checker.canPlayerAffect(player, vehicle)) return;
            event.setCancelled(true);
            ProtectionNotifier.notify(player, Message.PROTECTION_VEHICLE_DAMAGE, vehiclePlot);
            return;
        } else {
            Plot attackerPlot = checker.getPlot(attacker);
            if (checker.canPlotAffectPlot(attackerPlot, vehiclePlot)) return;
        }

        event.setCancelled(true);
    }
}
