package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class LecternBookProtection implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTakeLecternBook(PlayerTakeLecternBookEvent event){
		final Location lecternLocation = event.getLectern().getLocation();
		final Player player = event.getPlayer();
		final Plot eventPlot = PlotManager.getInstance().getPlot(lecternLocation.getChunk());

		event.setCancelled(!ProtectionUtil.canPlayerAffectPlot(player, eventPlot));
	}
}
