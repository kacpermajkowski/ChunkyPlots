package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class PlayerLeashEntityProtection implements Listener {
    @EventHandler
    public void onPlayerLeashEntity(final PlayerLeashEntityEvent event){
        final Location entityLocation = event.getEntity().getLocation();
        final Player player = event.getPlayer();
        final Plot eventPlot = PlotManager.getInstance().getPlot(entityLocation);

        event.setCancelled(!ProtectionUtil.canPlayerAffectPlot(player, eventPlot));
    }
}
