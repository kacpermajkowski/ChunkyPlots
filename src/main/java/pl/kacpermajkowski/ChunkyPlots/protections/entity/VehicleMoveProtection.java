package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.ChunkBorderUtil;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class VehicleMoveProtection implements Listener {
    private final PlotPermissionChecker checker;

    public VehicleMoveProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleMove(VehicleMoveEvent event) {
        if(event.getTo() == null || event.getFrom().distance(event.getTo()) == 0) return;

        Plot toPlot = checker.getPlot(event.getTo());
        if(toPlot == null) return;

        Vehicle vehicle = event.getVehicle();
        for(Entity entity : vehicle.getPassengers()) {
            if(entity instanceof Player player){
                if(checker.canPlayerAffect(player, toPlot)) return;
            }
        }

        Plot fromPlot = checker.getPlot(event.getFrom());
        boolean isFromSafe = !toPlot.equals(fromPlot);
        if (isFromSafe && fromPlot != null) {
            for (Entity entity : vehicle.getPassengers()) {
                if (entity instanceof Player player) {
                    if (fromPlot.isPlayerBlacklisted(player) && !checker.canPlayerAffect(player, fromPlot)) {
                        isFromSafe = false;
                        break;
                    }
                }
            }
        }

        Location safeLocation;
        if (isFromSafe) {
            safeLocation = event.getFrom().clone();
        } else {
            safeLocation = ChunkBorderUtil.getSafeBorderPoint(vehicle, toPlot.getChunkX(), toPlot.getChunkZ(), checker);
        }

        if (safeLocation != null) {
            safeLocation.setYaw(event.getTo().getYaw());
            safeLocation.setPitch(event.getTo().getPitch());
            vehicle.teleport(safeLocation);
        }
    }
}
