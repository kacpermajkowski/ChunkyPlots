package pl.kacpermajkowski.ChunkyPlots.protections.redstone;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;

public class DispenserProtection implements Listener {
	@EventHandler
	public void onDispense(BlockDispenseEvent event){
		if(!canBlockDispense(event)){
			event.setCancelled(true);
		}
	}

	private boolean canBlockDispense(BlockDispenseEvent event) {
		Plot sourcePlot = getEventSourcePlot(event);
		Plot destinationPlot = getEventDestinationPlot(event);
		return canPlotDispenseOnPlot(sourcePlot, destinationPlot);
	}

	private Plot getEventSourcePlot(BlockDispenseEvent event) {
		Block source = event.getBlock();
		return ChunkyPlots.getInstance().plotManager.getPlotByChunk(source.getChunk());
	}

	private Plot getEventDestinationPlot(BlockDispenseEvent event) {
		int x = event.getVelocity().getBlockX();
		int y = event.getVelocity().getBlockY();
		int z = event.getVelocity().getBlockZ();
		World world = event.getBlock().getWorld();
		Block destination = world.getBlockAt(x, y, z);

		return ChunkyPlots.getInstance().plotManager.getPlotByChunk(destination.getChunk());
	}

	private boolean canPlotDispenseOnPlot(Plot sourcePlot, Plot destinationPlot) {
		if(sourcePlot == null && destinationPlot != null){
			return false;
		} else if(sourcePlot != null && destinationPlot != null) {
			return sourcePlot.getOwnerNickname().equals(destinationPlot.getOwnerNickname());
		}
		return true;
	}
}
