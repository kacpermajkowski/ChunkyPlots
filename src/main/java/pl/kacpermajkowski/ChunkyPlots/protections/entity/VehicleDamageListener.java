package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class VehicleDamageListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleDamage(final VehicleDamageEvent event){
        final Entity attacker = event.getAttacker();
        final Entity vehicle = event.getVehicle();

        if(attacker instanceof Player player) {
            if (ProtectionUtil.canPlayerAffect(player, vehicle)) return;
        } else  {
            Plot entityPlot = PlotManager.getInstance().getPlot(vehicle);
            Plot attackerPlot = PlotManager.getInstance().getPlot(attacker);
            if (ProtectionUtil.canPlotAffectPlot(attackerPlot, entityPlot)) return;
        }

        event.setCancelled(true);
    }

}
