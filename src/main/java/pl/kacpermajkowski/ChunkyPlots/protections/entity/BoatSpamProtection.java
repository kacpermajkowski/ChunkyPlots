package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

import static pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil.getSafeBorderPoint;

public class BoatSpamProtection implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleExit(VehicleExitEvent event) {
        Plot plot = PlotManager.getInstance().getPlot(event.getVehicle().getLocation());
        if (plot == null) return;

        if(!(event.getExited() instanceof Player player)) return;
        else if (ProtectionUtil.canPlayerAffectPlot(player, plot)) return;

        event.getVehicle().remove();
    }
}
