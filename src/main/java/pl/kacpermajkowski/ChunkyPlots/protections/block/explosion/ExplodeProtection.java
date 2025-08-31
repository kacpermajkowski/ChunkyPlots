package pl.kacpermajkowski.ChunkyPlots.protections.block.explosion;

import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

public class ExplodeProtection implements Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	public void onExplode(EntityExplodeEvent event) {
		// By default, all explosions except Wind Charges will be cancelled unless the plugin allows them to happen
		if(event.getEntity() instanceof WindCharge) return;
		else event.setCancelled(true);
	}
}
