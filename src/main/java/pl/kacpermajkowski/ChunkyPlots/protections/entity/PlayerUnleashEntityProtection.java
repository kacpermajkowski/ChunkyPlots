package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class PlayerUnleashEntityProtection implements Listener {
	@EventHandler
	public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event){
		final Location entityLocation = event.getEntity().getLocation();
		final Player player = event.getPlayer();
		final Plot eventPlot = PlotManager.getInstance().getPlot(entityLocation);

		event.setCancelled(!ProtectionUtil.canPlayerAffectPlot(player, eventPlot));
	}
}
