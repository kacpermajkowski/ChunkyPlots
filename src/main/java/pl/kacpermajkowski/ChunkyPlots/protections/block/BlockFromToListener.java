package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

public class BlockFromToListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockFromTo(BlockFromToEvent event){
		if(!canBlockMoveFromTo(event.getBlock(), event.getToBlock())){
			event.setCancelled(true);
		}
	}

	private boolean canBlockMoveFromTo(Block fromBlock, Block toBlock) {
		//This event is handling only two types of blocks: liquids and dragon eggs
        if (fromBlock.getType() == Material.DRAGON_EGG) {
            return canDragonEggMoveFromTo(fromBlock, toBlock);
        }
        return canLiquidMoveFromTo(fromBlock, toBlock);
    }

	private boolean canLiquidMoveFromTo(Block fromBlock, Block toBlock) {
		Plot fromPlot = PlotManager.getInstance().getPlot(fromBlock);
		Plot toPlot = PlotManager.getInstance().getPlot(toBlock);
		if(fromPlot == null && toPlot == null) return true;
		else if(fromPlot != null && toPlot == null) return true;
		else if(fromPlot == null && toPlot != null) return false;
		else return fromPlot.hasTheSameOwnerAs(toPlot);
	}

	private boolean canDragonEggMoveFromTo(Block fromBlock, Block toBlock) {
		Plot fromPlot = PlotManager.getInstance().getPlot(fromBlock);
		Plot toPlot = PlotManager.getInstance().getPlot(toBlock);
		if(fromPlot == null && toPlot == null) return true;
		else if(fromPlot != null && toPlot == null) return false;
		else if(fromPlot == null && toPlot != null) return false;
		else return fromPlot.hasTheSameOwnerAs(toPlot);
	}
}
