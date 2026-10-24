package pl.kacpermajkowski.ChunkyPlots.protections.block.explosion;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class ExplodeProtection implements Listener {
	private final PlotPermissionChecker checker;

	public ExplodeProtection() {
		this(null);
	}

	public ExplodeProtection(PlotPermissionChecker checker) {
		this.checker = checker;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onExplode(EntityExplodeEvent event) {
		if(event.getEntity() instanceof WindCharge) return;
		if (checker != null) {
			Plot plot = checker.getPlot(event.getLocation());
			if (plot != null && plot.getFlag(PlotFlag.EXPLOSIONS)) {
				return;
			}
		}
		event.setCancelled(true);
	}
}
