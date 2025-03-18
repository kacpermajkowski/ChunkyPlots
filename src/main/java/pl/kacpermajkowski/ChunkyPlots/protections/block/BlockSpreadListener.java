package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

public class BlockSpreadListener implements Listener {
	@EventHandler
	public void onBlockSpread(BlockSpreadEvent event){
		Plot sourceBlockPlot = PlotManager.getInstance().getPlot(event.getSource());
		Plot destinationBlockPlot = PlotManager.getInstance().getPlot(event.getBlock());
		if(!canBlockSpreadFromPlotToPlot(sourceBlockPlot, destinationBlockPlot)){
			event.setCancelled(true);
		}
	}

	// TODO: Reevaluate why can't block spread from a plot to unclaimed chunks
	private boolean canBlockSpreadFromPlotToPlot(Plot sourcePlot, Plot destinationPlot) {
		if(sourcePlot == null && destinationPlot == null) return true;
		else if(sourcePlot != null && destinationPlot != null){
			return sourcePlot.hasTheSameOwnerAs(destinationPlot);
		}
		return false;
	}
}
