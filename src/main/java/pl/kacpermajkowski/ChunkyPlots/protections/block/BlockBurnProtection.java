package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class BlockBurnProtection implements Listener {
	private final PlotPermissionChecker checker;

	public BlockBurnProtection(PlotPermissionChecker checker) {
		this.checker = checker;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBurn(BlockBurnEvent event){
		if(!blockCanBeBurntByFire(event.getBlock(), event.getIgnitingBlock())){
			event.setCancelled(true);
		}
 	}

	private boolean blockCanBeBurntByFire(Block block, Block fire) {
		Plot blockPlot = checker.getPlot(block);
		if(blockPlot != null && blockPlot.getFlag(PlotFlag.FIRE_SPREAD)) return true;
		if(fire == null) return false;
		Plot firePlot = checker.getPlot(fire);
		if(blockPlot == null) return true;
		if(firePlot == null) return false;
		return blockPlot.hasTheSameOwnerAs(firePlot);
	}
}
