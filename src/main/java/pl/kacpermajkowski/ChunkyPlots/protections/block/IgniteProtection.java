package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class IgniteProtection implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockIgnite(BlockIgniteEvent event){
		if(!canBlockBeIgnited(event)){
			event.setCancelled(true);
		}
	}

	private boolean canBlockBeIgnited(BlockIgniteEvent event) {
		Block block = event.getBlock();

		Block ignitingBlock = event.getIgnitingBlock();
		Entity ignitingEntity = event.getIgnitingEntity();

		if(ignitingBlock != null){
			return canBlockBeIgnitedByBlock(block, ignitingBlock);
		} else if(ignitingEntity != null){
			return canBlockBeIgnitedByEntity(block, ignitingEntity);
		} else {
			return false;
		}
	}

	private boolean canBlockBeIgnitedByBlock(Block block, Block ignitingBlock) {
		Chunk ignitingBlockChunk = ignitingBlock.getChunk();
		Chunk blockChunk = block.getChunk();
		Plot sourceBlockPlot = PlotManager.getInstance().getPlot(ignitingBlockChunk);
		Plot destinationBlockPlot = PlotManager.getInstance().getPlot(blockChunk);

		if(sourceBlockPlot != null && destinationBlockPlot != null){
			return sourceBlockPlot.hasTheSameOwnerAs(destinationBlockPlot);
		} else if(sourceBlockPlot != null && destinationBlockPlot == null){
			return true;
		} else if(sourceBlockPlot == null && destinationBlockPlot != null){
			return false;
		} else {
			return true;
		}
	}

	private boolean canBlockBeIgnitedByEntity(Block block, Entity ignitingEntity) {
		Chunk blockChunk = block.getChunk();
		Plot destinationBlockPlot = PlotManager.getInstance().getPlot(blockChunk);

		if (destinationBlockPlot != null) {
			if (ignitingEntity instanceof Player player) {
				return ProtectionUtil.canPlayerAffectPlot(player, destinationBlockPlot);
			} else if (ignitingEntity instanceof EnderCrystal enderCrystal) {
				return canEnderCrystalIgnitePlot(enderCrystal, destinationBlockPlot);
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	private boolean canEnderCrystalIgnitePlot(EnderCrystal enderCrystal, Plot plot) {
		Location enderCrystalLocation = enderCrystal.getLocation();
		return plot.isLocationInside(enderCrystalLocation);
	}
}
