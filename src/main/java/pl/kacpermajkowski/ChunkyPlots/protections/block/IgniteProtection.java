package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class IgniteProtection implements Listener {
	private final PlotPermissionChecker checker;

	public IgniteProtection(PlotPermissionChecker checker) {
		this.checker = checker;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockIgnite(BlockIgniteEvent event){
		if(!canBlockBeIgnited(event)){
			event.setCancelled(true);
			if (event.getIgnitingEntity() instanceof Player player) {
				Plot plot = checker.getPlot(event.getBlock());
				ProtectionNotifier.notify(player, Message.PROTECTION_IGNITE, plot);
			}
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
		Plot sourceBlockPlot = checker.getPlot(ignitingBlock);
		Plot destinationBlockPlot = checker.getPlot(block);

		if(destinationBlockPlot != null && destinationBlockPlot.getFlag(PlotFlag.FIRE_SPREAD)) {
			return true;
		}

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
		Plot destinationBlockPlot = checker.getPlot(block);

		if (destinationBlockPlot == null) {
			return true;
		}
		if (destinationBlockPlot.getFlag(PlotFlag.FIRE_SPREAD)) {
			return true;
		}
		if (ignitingEntity instanceof Player player) {
			return checker.canPlayerAffect(player, destinationBlockPlot);
		} else if (ignitingEntity instanceof EnderCrystal enderCrystal) {
			return canEnderCrystalIgnitePlot(enderCrystal, destinationBlockPlot);
		} else {
			return false;
		}
	}

	private boolean canEnderCrystalIgnitePlot(EnderCrystal enderCrystal, Plot plot) {
		Location enderCrystalLocation = enderCrystal.getLocation();
		return plot.isLocationInside(enderCrystalLocation);
	}
}
