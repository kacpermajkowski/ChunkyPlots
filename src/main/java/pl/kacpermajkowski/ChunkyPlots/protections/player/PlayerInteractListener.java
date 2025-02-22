package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class PlayerInteractListener implements Listener {
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		final Block block = event.getClickedBlock();
		final Player player = event.getPlayer();
		final Plot eventPlot;
		if(block != null) {
			eventPlot = PlotManager.getInstance().getPlot(block.getChunk());
			if (eventPlot != null) {
				if (!PlotPermissionUtil.canPlayerAffectPlot(player, eventPlot)) {
					event.setCancelled(true);
				}
			}
		}
	}
}
