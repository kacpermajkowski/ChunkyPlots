package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.Flag;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class VehicleEnterListener implements Listener {
    @EventHandler
    public void onVehicleEnter(final VehicleEnterEvent event) {
        final Entity entered = event.getEntered();

        final Location vehicleLocation = event.getVehicle().getLocation();
        final Plot eventPlot = ChunkyPlots.getInstance().plotManager.getPlotByChunk(vehicleLocation.getChunk());

        if(entered instanceof Player player) {
            if(eventPlot != null) {
                if (!PlotPermissionUtil.canPlayerAffectPlot(player, eventPlot, Flag.VEHICLE_ENTER_MEMBER, Flag.VEHICLE_ENTER_STRANGER)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
