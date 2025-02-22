package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class PlayerShearEntityProtection implements Listener {
    @EventHandler
    public void onPlayerShearEntity(final PlayerShearEntityEvent event){
        final Location entityLocation = event.getEntity().getLocation();
        final Player player = event.getPlayer();
        final Plot eventPlot = PlotManager.getInstance().getPlot(entityLocation.getChunk());

        if(eventPlot != null) {
            if (!PlotPermissionUtil.canPlayerAffectPlot(player, eventPlot)) {
                event.setCancelled(true);
            }
        }
    }
}
