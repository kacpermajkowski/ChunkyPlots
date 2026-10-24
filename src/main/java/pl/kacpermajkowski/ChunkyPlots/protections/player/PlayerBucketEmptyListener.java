package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class PlayerBucketEmptyListener implements Listener {
	private final PlotPermissionChecker checker;

	public PlayerBucketEmptyListener(PlotPermissionChecker checker) {
		this.checker = checker;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event){
		final Block block = event.getBlockClicked();
		final Player player = event.getPlayer();
		final Plot eventPlot = checker.getPlot(block.getChunk());

		event.setCancelled(!checker.canPlayerAffect(player, eventPlot));
	}
}
