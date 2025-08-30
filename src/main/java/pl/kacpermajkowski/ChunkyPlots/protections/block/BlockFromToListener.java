package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;

public class BlockFromToListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockFromTo(BlockFromToEvent event){
		if(!canBlockMoveFromTo(event.getBlock(), event.getToBlock())){
			event.setCancelled(true);
		}
	}

	private boolean canBlockMoveFromTo(Block block, Block toBlock) {
		//This event is handling only two types of blocks: liquids and dragon eggs
		if(block.getType().equals(Material.DRAGON_EGG)){
			return canDragonEggMoveFromTo(block, toBlock);
		} else {
			return canLiquidMoveFromTo(block, toBlock);
		}
	}

	private boolean canLiquidMoveFromTo(Block block, Block toBlock) {
		Plot fromPlot = PlotManager.getInstance().getPlot(block.getChunk());
		Plot toPlot = PlotManager.getInstance().getPlot(toBlock.getChunk());
		if(fromPlot == null && toPlot == null) {
			return true;
		} else if(fromPlot != null && toPlot == null){
			return true;
		} else if(fromPlot == null && toPlot != null){
			return false;
		} else return fromPlot.hasTheSameOwnerAs(toPlot);
	}

	private boolean canDragonEggMoveFromTo(Block block, Block toBlock) {
		Plot fromPlot = PlotManager.getInstance().getPlot(block.getChunk());
		Plot toPlot = PlotManager.getInstance().getPlot(toBlock.getChunk());
		if(fromPlot == null && toPlot == null) {
			return true;
		} else if(fromPlot != null && toPlot == null){
			return false;
		} else if(fromPlot == null && toPlot != null){
			return false;
		} else return fromPlot.hasTheSameOwnerAs(toPlot);
	}
}
