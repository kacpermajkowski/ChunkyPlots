package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.Flag;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class PlayerBucketEmptyListener implements Listener {
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event){
		final Block block = event.getBlockClicked();
		final Player player = event.getPlayer();
		final Plot eventPlot = ChunkyPlots.getInstance().plotManager.getPlotByChunk(block.getChunk());

		if(eventPlot != null) {
			if (!PlotPermissionUtil.canPlayerAffectPlot(player, eventPlot, Flag.PLACE_MEMBER, Flag.PLACE_STRANGER)) {
				event.setCancelled(true);
			}
		}
	}
}
