package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class VehicleDamageListener implements Listener {
    @EventHandler
    public void onVehicleDamage(final VehicleDamageEvent event){
        final Entity attacker = event.getAttacker();

        final Location vehicleLocation = event.getVehicle().getLocation();
        final Plot eventPlot = PlotManager.getInstance().getPlot(vehicleLocation.getChunk());

        if(attacker instanceof Player player) {
            if(eventPlot != null) {
                if (!PlotPermissionUtil.canPlayerAffectPlot(player, eventPlot)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
