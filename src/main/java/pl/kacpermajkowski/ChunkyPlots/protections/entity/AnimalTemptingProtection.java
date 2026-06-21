package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class AnimalTemptingProtection implements Listener {
    @EventHandler
    public void onAnimalMove(EntityTargetEvent event){
        if(event.getReason() != EntityTargetEvent.TargetReason.TEMPT){
            return;
        }
        if(!(event.getTarget() instanceof Player player)){
            return;
        }

        Plot plot = PlotManager.getInstance().getPlot(event.getEntity());
        if(plot == null){
            return;
        }
        if(ProtectionUtil.canPlayerAffect(player,plot)){
            return;
        }

        event.setCancelled(true);
    }
}
