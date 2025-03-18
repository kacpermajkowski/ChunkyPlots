package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class PlayerBucketEmptyListener implements Listener {
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event){
		final Block block = event.getBlockClicked();
		final Player player = event.getPlayer();
		final Plot eventPlot = PlotManager.getInstance().getPlot(block.getChunk());

		event.setCancelled(!ProtectionUtil.canPlayerAffectPlot(player, eventPlot));
	}
}
