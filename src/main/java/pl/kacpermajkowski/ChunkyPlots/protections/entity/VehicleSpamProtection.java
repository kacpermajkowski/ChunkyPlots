package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class VehicleSpamProtection implements Listener {
    private final PlotPermissionChecker checker;

    public VehicleSpamProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleExit(VehicleExitEvent event) {
        Plot plot = checker.getPlot(event.getVehicle());
        if (plot == null) return;

        if(!(event.getExited() instanceof Player player)) return;
        else if (checker.canPlayerAffect(player, plot)) return;

        event.getVehicle().remove();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleMove(VehicleMoveEvent event) {
        Plot destination = checker.getPlot(event.getTo());
        if (destination == null) return;

        boolean isAllowed;
        Plot source = checker.getPlot(event.getFrom());
        if (source == null){
            isAllowed = handleVehicleEnteringPlot(event, destination);
        } else {
            isAllowed = handleVehicleSwitchingPlots(event, source, destination);
        }

        if(isAllowed){
            return;
        }

        event.getVehicle().remove();
    }

    private boolean handleVehicleSwitchingPlots(VehicleMoveEvent event, Plot source, Plot destination) {
        if(checker.canPlotAffectPlot(source, destination)){
            return true;
        } else return handleVehicleEnteringPlot(event, destination);
    }

    private boolean handleVehicleEnteringPlot(VehicleMoveEvent event, Plot destination) {
        boolean result = false;
        for(Entity passenger: event.getVehicle().getPassengers()){
            if(passenger instanceof Player player){
                if(checker.canPlayerAffect(player, destination)) {
                    result = true;
                }
            }
        }
        return result;
    }
}
