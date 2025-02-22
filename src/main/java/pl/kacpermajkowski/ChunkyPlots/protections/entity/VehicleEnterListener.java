package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class VehicleEnterListener implements Listener {
    @EventHandler
    public void onVehicleEnter(final VehicleEnterEvent event) {
        final Entity entered = event.getEntered();

        final Location vehicleLocation = event.getVehicle().getLocation();
        final Plot eventPlot = PlotManager.getInstance().getPlot(vehicleLocation.getChunk());

        if(entered instanceof Player player) {
            if(eventPlot != null) {
                if (!PlotPermissionUtil.canPlayerAffectPlot(player, eventPlot)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
