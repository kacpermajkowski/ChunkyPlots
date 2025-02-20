package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.Flag;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class PlayerInteractAtEntityListener implements Listener {
	@EventHandler
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event){
		Entity clickedEntity = event.getRightClicked();
		Player player = event.getPlayer();
		Location clickedEntityLocation = clickedEntity.getLocation();
		Plot eventPlot = ChunkyPlots.getInstance().plotManager.getPlotByChunk(clickedEntityLocation.getChunk());

		if(eventPlot != null) {
			if (!PlotPermissionUtil.canPlayerAffectPlot(player, eventPlot, Flag.ENTITY_INTERACT_MEMBER, Flag.ENTITY_INTERACT_STRANGER)) {
				event.setCancelled(true);
			}
		}
	}
}
