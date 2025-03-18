package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

public class BlockBurnProtection implements Listener {

	@EventHandler
	public void onBlockBurn(BlockBurnEvent event){
		if(!blockCanBeBurntByFire(event.getBlock(), event.getIgnitingBlock())){
			event.setCancelled(true);
		}
 	}

	private boolean blockCanBeBurntByFire(Block block, Block fire) {
		if(fire == null) return false;
		Plot blockPlot = PlotManager.getInstance().getPlot(block.getChunk());
		Plot firePlot = PlotManager.getInstance().getPlot(fire.getChunk());
		if(blockPlot == null) return true;
		if(firePlot == null) return false;
		return blockPlot.hasTheSameOwnerAs(firePlot);
	}
}
