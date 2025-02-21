package pl.kacpermajkowski.ChunkyPlots.protections.misc;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.projectiles.ProjectileSource;
import pl.kacpermajkowski.ChunkyPlots.basic.Flag;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class LingeringPotionProtection implements Listener {

	@EventHandler
	public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event){
		if(!canEffectBeApplied(event)){
			event.setCancelled(true);
		}
	}
	private boolean canEffectBeApplied(AreaEffectCloudApplyEvent event){
		for(LivingEntity entity:event.getAffectedEntities()){
			ProjectileSource shooter = event.getEntity().getSource();
			return canShooterApplyEffectToEntity(shooter, entity);
		}
		return false;
	}

	private boolean canShooterApplyEffectToEntity(ProjectileSource shooter, LivingEntity entity) {
		Plot plotEntityIsStandingOn = PlotManager.getInstance().getPlot(entity.getLocation().getChunk());
		if(plotEntityIsStandingOn != null){
			return canShooterApplyEffectToEntityStandingOnPlot(shooter, plotEntityIsStandingOn);
		}
		return true;
	}

	private boolean canShooterApplyEffectToEntityStandingOnPlot(ProjectileSource shooter, Plot plotEntityIsStandingOn) {
		if(shooter instanceof Player player){
			return PlotPermissionUtil.canPlayerAffectPlot(player, plotEntityIsStandingOn, Flag.SPLASH_POTION_MEMBER, Flag.SPLASH_POTION_STRANGER);
		}
		return false;
	}
}
