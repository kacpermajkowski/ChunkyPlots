package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class PlayerInteractListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(final PlayerInteractEvent event){
		switch (event.getAction()){
			case PHYSICAL:
				handlePhysicalInteraction(event);
				break;
			case RIGHT_CLICK_BLOCK:
			case LEFT_CLICK_BLOCK:
				handleBlockClickInteration(event);
				break;
		}
	}

	private void handleBlockClickInteration(final PlayerInteractEvent event) {
		final Block block = event.getClickedBlock();
		final Player player = event.getPlayer();
		final Plot eventPlot;
		if(block != null) {
			eventPlot = PlotManager.getInstance().getPlot(block.getChunk());
			if(ProtectionUtil.canPlayerAffect(player, eventPlot)) return;
			event.setCancelled(true);
		}
	}

	//redstone ore light up protection
	private void handlePhysicalInteraction(final PlayerInteractEvent event) {
		if (!event.hasBlock()) return;

		final Block block = event.getClickedBlock();
		if (block == null) return;
		if (block.getType() != Material.REDSTONE_ORE) return;

		Plot plot = PlotManager.getInstance().getPlot(block);
		if (ProtectionUtil.canPlayerAffect(event.getPlayer(), plot)) return;
		event.setCancelled(true);

//		client-side prediction cosmetic effect fix, still works only when player drops off the ore
		Bukkit.getScheduler().runTaskLater(ChunkyPlots.instance(),
				() -> event.getPlayer().sendBlockChange(block.getLocation(), block.getBlockData())
		, 1L);
	}
}
