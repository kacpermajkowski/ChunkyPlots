package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class PlayerInteractAtEntityListener implements Listener {
	@EventHandler
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event){
		Entity clickedEntity = event.getRightClicked();
		Player player = event.getPlayer();
		Location clickedEntityLocation = clickedEntity.getLocation();
		Plot eventPlot = PlotManager.getInstance().getPlot(clickedEntityLocation.getChunk());

		event.setCancelled(!ProtectionUtil.canPlayerAffectPlot(player, eventPlot));
	}
}
