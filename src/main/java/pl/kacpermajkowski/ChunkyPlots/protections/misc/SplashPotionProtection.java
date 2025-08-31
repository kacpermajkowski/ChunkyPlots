package pl.kacpermajkowski.ChunkyPlots.protections.misc;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SplashPotionProtection implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPotionSplash(final PotionSplashEvent event){
		if(!canPotionBeSplashed(event)){
			event.setCancelled(true);
		}
	}
	private boolean canPotionBeSplashed(final PotionSplashEvent event) {
		ProjectileSource shooter = event.getPotion().getShooter();
		if(shooter instanceof Player){
			return canPlayerSplashPotion(event);
		} else if(shooter instanceof BlockProjectileSource){
			return canBlockSplashPotion(event);
		} else if(shooter instanceof LivingEntity){
			return canLivingEntitySplashPotion(event);
		}
		return false;
	}
	private boolean canPlayerSplashPotion(final PotionSplashEvent event) {
		Player player = (Player) event.getPotion().getShooter();
		List<Plot> affectedEntitesPlots = getAffectedEntitiesPlotList(event);
		for(Plot plot:affectedEntitesPlots) {
			if (!ProtectionUtil.canPlayerAffect(player, plot)){
				return false;
			}
		}
		return true;
	}
	private boolean canBlockSplashPotion(final PotionSplashEvent event) {
		BlockProjectileSource blockProjectileSource = (BlockProjectileSource) event.getPotion().getShooter();
		Block block = blockProjectileSource.getBlock();
		Plot blockPlot = PlotManager.getInstance().getPlot(block.getChunk());

		return canPlotSplashPotion(blockPlot, event);
	}
	private boolean canLivingEntitySplashPotion(final PotionSplashEvent event) {
		LivingEntity livingEntity = (LivingEntity) event.getPotion().getShooter();
		Plot plot = PlotManager.getInstance().getPlot(livingEntity.getLocation());

		return canPlotSplashPotion(plot, event);
	}
	private List<Plot> getAffectedEntitiesPlotList(final PotionSplashEvent event){
		List<Plot> plots = new ArrayList<>();
		Collection<LivingEntity> affectedEntities = event.getAffectedEntities();

		for(LivingEntity entity:affectedEntities){
			Plot plot = PlotManager.getInstance().getPlot(entity.getLocation());
			if(plot != null){
				if(!plots.contains(plot)) {
					plots.add(plot);
				}
			}
		}
		return plots;
	}
	private boolean canPlotSplashPotion(final Plot plot, final  PotionSplashEvent event){
		final List<Plot> affectedEntitesPlots = getAffectedEntitiesPlotList(event);
		if(plot != null) {
			return plot.hasTheSameOwnerAs(affectedEntitesPlots);
		} else {
			return affectedEntitesPlots.isEmpty();
		}
	}

	@EventHandler
	public void onLingeringPotionSplash(final LingeringPotionSplashEvent event){
		if(!canLingeringPotionBeSplashed(event)){
			event.setCancelled(true);
		}
	}
	private boolean canLingeringPotionBeSplashed(LingeringPotionSplashEvent event) {
		ProjectileSource shooter = event.getEntity().getShooter();
		if(shooter instanceof Player){
			return canPlayerSplashLingeringPotion(event);
		} else if(shooter instanceof BlockProjectileSource){
			return canBlockSplashLingeringPotion(event);
		} else if(shooter instanceof LivingEntity){
			return canLivingEntitySplashLingeringPotion(event);
		}
		return false;
	}
	private boolean canPlayerSplashLingeringPotion(LingeringPotionSplashEvent event) {
		ProjectileSource shooter = event.getEntity().getShooter();
		Player player = (Player) shooter;

		Plot plot = PlotManager.getInstance().getPlot(event.getHitBlock().getLocation());

		if(plot != null) {
			return ProtectionUtil.canPlayerAffect(player, plot);
		} else {
			return true;
		}
	}
	private boolean canBlockSplashLingeringPotion(LingeringPotionSplashEvent event) {
		final BlockProjectileSource blockProjectileSource = (BlockProjectileSource) event.getEntity().getShooter();
		if(blockProjectileSource == null) return false;

		final Block blockShooter = blockProjectileSource.getBlock();
		final Location splashLocation = event.getHitBlock() == null ? event.getHitBlock().getLocation() : event.getHitEntity().getLocation();

		final Plot sourcePlot = PlotManager.getInstance().getPlot(blockShooter);
		final Plot eventPlot = PlotManager.getInstance().getPlot(splashLocation);

		if(eventPlot == null) {
			return true;
		}

		return sourcePlot.hasTheSameOwnerAs(eventPlot);
	}
	private boolean canLivingEntitySplashLingeringPotion(LingeringPotionSplashEvent event) {
		final ProjectileSource shooter = event.getEntity().getShooter();
		final LivingEntity livingEntity = (LivingEntity) shooter;
		final Plot sourcePlot = PlotManager.getInstance().getPlot(livingEntity.getLocation());

		final Location splashLocation = event.getHitBlock() == null ? event.getHitBlock().getLocation() : event.getHitEntity().getLocation();
		final Plot eventPlot = PlotManager.getInstance().getPlot(splashLocation);

		if(eventPlot == null) return true;
		if(sourcePlot == null) return false;

		return eventPlot.hasTheSameOwnerAs(sourcePlot);
	}
}
