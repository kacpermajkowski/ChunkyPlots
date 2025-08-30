package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class EntityInteractionProtection implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event){
		Entity clickedEntity = event.getRightClicked();
		Player player = event.getPlayer();
		Location clickedEntityLocation = clickedEntity.getLocation();
		Plot eventPlot = PlotManager.getInstance().getPlot(clickedEntityLocation);

		if(eventPlot != null) {
			if (!PlotPermissionUtil.canPlayerAffectPlot(player, eventPlot)) {
				event.setCancelled(true);
			}
		}
	}
}
