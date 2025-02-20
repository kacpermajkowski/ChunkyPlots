package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.Flag;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;

public class BlockBurnProtection implements Listener {

	@EventHandler
	public void onBlockBurn(BlockBurnEvent event){
		if(!blockCanBeBurntByFire(event.getBlock(), event.getIgnitingBlock())){
			event.setCancelled(true);
		}
 	}

	private boolean blockCanBeBurntByFire(Block block, Block fire) {
		if(fire != null) {
			Plot blockPlot = ChunkyPlots.getInstance().plotManager.getPlotByChunk(block.getChunk());
			Plot firePlot = ChunkyPlots.getInstance().plotManager.getPlotByChunk(fire.getChunk());
			if(blockPlot != null) {
				if(firePlot != null) {
					if (blockPlot.hasTheSameOwnerAs(firePlot)) {
						return true;
					} else {
						return !blockPlot.flags.get(Flag.EXTERNAL_FIRE_PROTECTION);
					}
				} else {
					return false;
				}
			}
		}
		return true;
	}
}
