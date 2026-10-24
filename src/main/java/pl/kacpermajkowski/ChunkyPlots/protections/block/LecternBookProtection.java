package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class LecternBookProtection implements Listener {
	private final PlotPermissionChecker checker;

	public LecternBookProtection(PlotPermissionChecker checker) {
		this.checker = checker;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTakeLecternBook(PlayerTakeLecternBookEvent event){
		final Location lecternLocation = event.getLectern().getLocation();
		final Player player = event.getPlayer();
		final Plot eventPlot = checker.getPlot(lecternLocation);

		if (!checker.canPlayerAffect(player, eventPlot)) {
			event.setCancelled(true);
			ProtectionNotifier.notify(player, Message.PROTECTION_LECTERN, eventPlot);
		}
	}
}
