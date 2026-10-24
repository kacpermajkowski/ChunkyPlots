package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class VehicleDamageListener implements Listener {
    private final PlotPermissionChecker checker;

    public VehicleDamageListener(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleDamage(final VehicleDamageEvent event){
        final Entity attacker = event.getAttacker();
        final Entity vehicle = event.getVehicle();

        if(attacker instanceof Player player) {
            if (checker.canPlayerAffect(player, vehicle)) return;
        } else {
            Plot entityPlot = checker.getPlot(vehicle);
            Plot attackerPlot = checker.getPlot(attacker);
            if (checker.canPlotAffectPlot(attackerPlot, entityPlot)) return;
        }

        event.setCancelled(true);
    }
}
