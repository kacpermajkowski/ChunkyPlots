package pl.kacpermajkowski.ChunkyPlots.protections.redstone;

import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class DispenserProtection implements Listener {
	private final PlotPermissionChecker checker;

	public DispenserProtection(PlotPermissionChecker checker) {
		this.checker = checker;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDispense(BlockDispenseEvent event){
		if(!canBlockDispense(event)){
			event.setCancelled(true);
		}
	}

	private boolean canBlockDispense(BlockDispenseEvent event) {
		Block source = event.getBlock();
		Plot sourcePlot = checker.getPlot(source);

		Block destination = getDestinationBlock(source);
		Plot destinationPlot = checker.getPlot(destination);

		return canPlotDispenseOnPlot(sourcePlot, destinationPlot);
	}

	private Block getDestinationBlock(Block source) {
		if (source.getBlockData() instanceof Directional directional) {
			return source.getRelative(directional.getFacing());
		}
		return source;
	}

	private boolean canPlotDispenseOnPlot(Plot sourcePlot, Plot destinationPlot) {
		if (destinationPlot == null) {
			return true; // Dispensing into wilderness is allowed
		}
		if (destinationPlot.getFlag(PlotFlag.DISPENSER)) {
			return true;
		}
		if (sourcePlot == null) {
			return false; // Wilderness dispenser cannot dispense into claimed plot
		}
		return checker.canPlotAffectPlot(sourcePlot, destinationPlot);
	}
}
