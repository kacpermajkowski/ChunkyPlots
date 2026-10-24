package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class BlockBreakProtection implements Listener {
	private final PlotPermissionChecker checker;

	public BlockBreakProtection(PlotPermissionChecker checker) {
		this.checker = checker;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(final BlockBreakEvent event){
		final Plot eventPlot = checker.getPlot(event.getBlock());
		final Player player = event.getPlayer();

		if (eventPlot != null && eventPlot.getFlag(PlotFlag.BLOCK_BREAK)) return;

		if (!checker.canPlayerAffect(player, eventPlot)) {
			event.setCancelled(true);
			ProtectionNotifier.notify(player, Message.PROTECTION_BLOCK_BREAK, eventPlot);
		}
	}
}
