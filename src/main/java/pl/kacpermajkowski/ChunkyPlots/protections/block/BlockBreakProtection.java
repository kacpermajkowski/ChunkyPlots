package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import pl.kacpermajkowski.ChunkyPlots.basic.Flag;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class BlockBreakProtection implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(final BlockBreakEvent event){
		if(!canPlayerDestroyBlock(event)){
			event.setCancelled(true);
		}
	}

	private boolean canPlayerDestroyBlock(BlockBreakEvent event) {
		Plot blockPlot = PlotManager.getInstance().getPlot(event.getBlock().getChunk());
		if(blockPlot != null){
			return PlotPermissionUtil.canPlayerAffectPlot(event.getPlayer(), blockPlot, Flag.BREAK_MEMBER, Flag.BREAK_STRANGER);
		} else {
			return true;
		}
	}
}
