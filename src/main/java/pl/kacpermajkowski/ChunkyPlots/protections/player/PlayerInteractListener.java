package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class PlayerInteractListener implements Listener {
	private final PlotPermissionChecker checker;
	private final Plugin plugin;

	public PlayerInteractListener(PlotPermissionChecker checker, Plugin plugin) {
		this.checker = checker;
		this.plugin = plugin;
	}

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
		if (block != null) {
			final Plot eventPlot = checker.getPlot(block.getChunk());
			if (checker.canPlayerAffect(player, eventPlot)) return;
			event.setCancelled(true);
			ProtectionNotifier.notify(player, Message.PROTECTION_BLOCK_INTERACT, eventPlot);
		}
	}

	private void handlePhysicalInteraction(final PlayerInteractEvent event) {
		if (!event.hasBlock()) return;

		final Block block = event.getClickedBlock();
		if (block == null) return;

		Material type = block.getType();
		Plot plot = checker.getPlot(block);
		if (checker.canPlayerAffect(event.getPlayer(), plot)) return;

		// Farmland trampling, pressure plates, tripwire hooks, and redstone ore
		if (type == Material.FARMLAND || type.name().endsWith("_PRESSURE_PLATE") || type == Material.TRIPWIRE) {
			event.setCancelled(true);
			ProtectionNotifier.notify(event.getPlayer(), Message.PROTECTION_CROP_TRAMPLE, plot);
			return;
		}

		if (type == Material.REDSTONE_ORE || type == Material.DEEPSLATE_REDSTONE_ORE) {
			event.setCancelled(true);
			ProtectionNotifier.notify(event.getPlayer(), Message.PROTECTION_CROP_TRAMPLE, plot);
			if (plugin != null && plugin.isEnabled()) {
				Bukkit.getScheduler().runTaskLater(plugin,
						() -> event.getPlayer().sendBlockChange(block.getLocation(), block.getBlockData()),
						1L);
			}
		}
	}
}
