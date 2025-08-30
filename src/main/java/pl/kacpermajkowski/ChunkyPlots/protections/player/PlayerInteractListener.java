package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class PlayerInteractListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event){
		final Block block = event.getClickedBlock();
		final Player player = event.getPlayer();
		final Plot eventPlot;
		if(block != null) {
			eventPlot = PlotManager.getInstance().getPlot(block.getChunk());
			event.setCancelled(!ProtectionUtil.canPlayerAffectPlot(player, eventPlot));
		}
	}
}
