package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class EntityInteractionProtection implements Listener {
	private final PlotPermissionChecker checker;

	public EntityInteractionProtection(PlotPermissionChecker checker) {
		this.checker = checker;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event){
		Entity clickedEntity = event.getRightClicked();
		Player player = event.getPlayer();
		Location clickedEntityLocation = clickedEntity.getLocation();
		Plot eventPlot = checker.getPlot(clickedEntityLocation);

		if (!checker.canPlayerAffect(player, eventPlot)) {
			event.setCancelled(true);
			ProtectionNotifier.notify(player, Message.PROTECTION_ENTITY_INTERACT, eventPlot);
		}
	}
}
