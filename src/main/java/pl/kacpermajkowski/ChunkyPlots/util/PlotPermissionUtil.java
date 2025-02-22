package pl.kacpermajkowski.ChunkyPlots.util;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;

import java.util.List;

public class PlotPermissionUtil {
	public static boolean canPlayerAffectPlot(Player player, Plot plot){
		if (plot == null) return true;
		else return plot.isPlayerOwner(player) || plot.isPlayerWhitelisted(player);
	}

	public static boolean canBlockAffectPlot(Block block, Plot plot){
		Plot blockPlot = PlotManager.getInstance().getPlot(block.getChunk());
		if(blockPlot != null) {
			return plot.hasTheSameOwnerAs(blockPlot);
		}
		return false;
	}

	public static boolean canBlockAffectBlock(Block block, Block block2){
		Plot blockPlot = PlotManager.getInstance().getPlot(block.getChunk());
		Plot block2Plot = PlotManager.getInstance().getPlot(block2.getChunk());
		if(blockPlot != null) {
			return blockPlot.hasTheSameOwnerAs(block2Plot);
		}
		return false;
	}

	public static boolean canPlotAffectPlots(Plot plot, List<Plot> plots){
		for(Plot affectedPlot:plots){
			if(plot != null) {
				return plot.hasTheSameOwnerAs(affectedPlot);
			} else {
				return false;
			}
		}
		return true;
	}

}
