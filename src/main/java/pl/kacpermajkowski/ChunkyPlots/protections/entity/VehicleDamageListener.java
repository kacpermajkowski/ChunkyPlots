package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.Flag;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class VehicleDamageListener implements Listener {
    @EventHandler
    public void onVehicleDamage(final VehicleDamageEvent event){
        final Entity attacker = event.getAttacker();

        final Location vehicleLocation = event.getVehicle().getLocation();
        final Plot eventPlot = ChunkyPlots.plugin.plotManager.getPlotByChunk(vehicleLocation.getChunk());

        if(attacker instanceof Player player) {
            if(eventPlot != null) {
                if (!PlotPermissionUtil.canPlayerAffectPlot(player, eventPlot, Flag.VEHICLE_DAMAGE_MEMBER, Flag.VEHICLE_DAMAGE_STRANGER)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
