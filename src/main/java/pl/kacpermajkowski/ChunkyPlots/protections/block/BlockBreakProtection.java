package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class BlockBreakProtection implements Listener {
	private final PlotPermissionChecker checker;

	public BlockBreakProtection(PlotPermissionChecker checker) {
		this.checker = checker;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(final BlockBreakEvent event){
		final Plot eventPlot = checker.getPlot(event.getBlock());
		final Player player = event.getPlayer();

		event.setCancelled(!checker.canPlayerAffect(player, eventPlot));
	}
}
