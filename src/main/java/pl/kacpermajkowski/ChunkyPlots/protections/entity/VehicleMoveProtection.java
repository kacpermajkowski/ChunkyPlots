package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

import static pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil.getSafeBorderPoint;

public class VehicleMoveProtection implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleMove(VehicleMoveEvent event) {
        if(event.getTo() == null || event.getFrom().distance(event.getTo()) == 0) return;

        Plot toPlot = PlotManager.getInstance().getPlot(event.getTo());
        if(toPlot == null) return;

        Vehicle vehicle = event.getVehicle();
        for(Entity entity : vehicle.getPassengers()) {
            if(entity instanceof Player player){
                if(ProtectionUtil.canPlayerAffect(player, toPlot)) return;
            }
        }

        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();
        int zDirection = fromChunk.getZ() - toChunk.getZ();
        int xDirection = fromChunk.getX() - toChunk.getX();

        if(zDirection == 0 && xDirection == 0) {
            Location closestBorderPoint = getSafeBorderPoint(vehicle, toChunk);
            closestBorderPoint.setYaw(event.getTo().getYaw());
            closestBorderPoint.setPitch(event.getTo().getPitch());

            vehicle.teleport(closestBorderPoint);
        } else {
            Vector pushVector = new Vector(xDirection, event.getTo().getDirection().getY() + 0.2, zDirection);
            vehicle.setVelocity(pushVector);
        }
    }
}
