package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class VehicleDamageListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleDamage(final VehicleDamageEvent event){
        final Entity attacker = event.getAttacker();

        final Location vehicleLocation = event.getVehicle().getLocation();
        final Plot eventPlot = PlotManager.getInstance().getPlot(vehicleLocation.getChunk());

        if(attacker instanceof Player player) {
            event.setCancelled(!ProtectionUtil.canPlayerAffect(player, eventPlot));
        }
    }
}
