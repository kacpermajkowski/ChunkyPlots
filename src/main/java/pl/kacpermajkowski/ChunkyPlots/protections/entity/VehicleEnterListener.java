package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class VehicleEnterListener implements Listener {
    private final PlotPermissionChecker checker;

    public VehicleEnterListener(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleEnter(final VehicleEnterEvent event) {
        final Entity entered = event.getEntered();

        final Location vehicleLocation = event.getVehicle().getLocation();
        final Plot eventPlot = checker.getPlot(vehicleLocation.getChunk());

        if(entered instanceof Player player) {
            if (!checker.canPlayerAffect(player, eventPlot)) {
                event.setCancelled(true);
                ProtectionNotifier.notify(player, Message.PROTECTION_VEHICLE_ENTER, eventPlot);
            }
        }
    }
}
