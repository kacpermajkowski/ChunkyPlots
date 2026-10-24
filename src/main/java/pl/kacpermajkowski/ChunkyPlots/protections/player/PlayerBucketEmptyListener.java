package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class PlayerBucketEmptyListener implements Listener {
	private final PlotPermissionChecker checker;

	public PlayerBucketEmptyListener(PlotPermissionChecker checker) {
		this.checker = checker;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event){
		Block targetBlock = event.getBlockClicked();
		if (targetBlock != null && event.getBlockFace() != null) {
			targetBlock = targetBlock.getRelative(event.getBlockFace());
		} else if (targetBlock == null) {
			targetBlock = event.getBlock();
		}
		final Player player = event.getPlayer();
		final Plot eventPlot = checker.getPlot(targetBlock);

		if (eventPlot != null && (eventPlot.getFlag(PlotFlag.BLOCK_PLACE) || eventPlot.getFlag(PlotFlag.LIQUID_FLOW))) return;

		if (!checker.canPlayerAffect(player, eventPlot)) {
			event.setCancelled(true);
			ProtectionNotifier.notify(player, Message.PROTECTION_BUCKET_EMPTY, eventPlot);
		}
	}
}
