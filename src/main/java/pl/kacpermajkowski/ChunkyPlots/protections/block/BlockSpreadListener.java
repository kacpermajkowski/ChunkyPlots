package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class BlockSpreadListener implements Listener {
	private final PlotPermissionChecker checker;

	public BlockSpreadListener(PlotPermissionChecker checker) {
		this.checker = checker;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockSpread(BlockSpreadEvent event){
		Plot sourceBlockPlot = checker.getPlot(event.getSource());
		Plot destinationBlockPlot = checker.getPlot(event.getBlock());
		if(!canBlockSpreadFromPlotToPlot(sourceBlockPlot, destinationBlockPlot)){
			event.setCancelled(true);
		}
	}

	private boolean canBlockSpreadFromPlotToPlot(Plot sourcePlot, Plot destinationPlot) {
		if (destinationPlot == null) return true; // Spreading into unclaimed wilderness is safe
		if (destinationPlot.getFlag(PlotFlag.FIRE_SPREAD)) return true;
		if (sourcePlot != null) {
			return sourcePlot.hasTheSameOwnerAs(destinationPlot);
		}
		return false; // Cannot spread from wilderness into a claimed plot
	}
}
