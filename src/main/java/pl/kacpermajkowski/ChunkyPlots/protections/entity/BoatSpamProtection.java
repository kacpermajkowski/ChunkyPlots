package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class BoatSpamProtection implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleExit(VehicleExitEvent event) {
        Plot plot = PlotManager.getInstance().getPlot(event.getVehicle().getLocation());
        if (plot == null) return;

        if(!(event.getExited() instanceof Player player)) return;
        else if (ProtectionUtil.canPlayerAffect(player, plot)) return;

        event.getVehicle().remove();
    }
}
