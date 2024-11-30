package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.Flag;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class PlayerInteractListener implements Listener {
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		final Block block = event.getClickedBlock();
		final Player player = event.getPlayer();
		final Plot eventPlot;
		if(block != null) {
			eventPlot = ChunkyPlots.plugin.plotManager.getPlotByChunk(block.getChunk());
			if (eventPlot != null) {
				if (!PlotPermissionUtil.canPlayerAffectPlot(player, eventPlot, Flag.BLOCK_INTERACT_MEMBER, Flag.BLOCK_INTERACT_STRANGER)) {
					event.setCancelled(true);
				}
			}
		}
	}
}
