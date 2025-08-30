package pl.kacpermajkowski.ChunkyPlots.protections.misc;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.projectiles.ProjectileSource;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class LingeringPotionProtection implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
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

	private boolean canShooterApplyEffectToEntityStandingOnPlot(ProjectileSource shooter, Plot entityPlot) {
		if(shooter instanceof Player player){
			return ProtectionUtil.canPlayerAffectPlot(player, entityPlot);
		}
		return false;
	}
}
