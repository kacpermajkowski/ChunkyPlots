package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class BlockBreakProtection implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(final BlockBreakEvent event){
		final Plot eventPlot = PlotManager.getInstance().getPlot(event.getBlock());
		final Player player = event.getPlayer();

		event.setCancelled(!ProtectionUtil.canPlayerAffect(player, eventPlot));
	}
}
