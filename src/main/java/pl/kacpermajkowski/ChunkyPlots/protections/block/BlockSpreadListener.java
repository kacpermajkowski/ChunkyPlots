package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;

public class BlockSpreadListener implements Listener {
	@EventHandler
	public void onBlockSpread(BlockSpreadEvent event){
		Plot sourceBlockPlot = ChunkyPlots.getInstance().plotManager.getPlotByChunk(event.getSource().getChunk());
		Plot destinationBlockPlot = ChunkyPlots.getInstance().plotManager.getPlotByChunk(event.getBlock().getChunk());
		if(!canBlockSpreadFromPlotToPlot(sourceBlockPlot, destinationBlockPlot)){
			event.setCancelled(true);
		}
	}

	private boolean canBlockSpreadFromPlotToPlot(Plot sourceBlockPlot, Plot destinationBlockPlot) {
		if(sourceBlockPlot == null && destinationBlockPlot == null) {
			return true;
		} else if(sourceBlockPlot != null && destinationBlockPlot != null){
			return sourceBlockPlot.hasTheSameOwnerAs(destinationBlockPlot);
		}
		return false;
	}
}
