package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ExplodeProtection implements Listener {
	private final HashMap<UUID, Player> witherSummoners = new HashMap<>();
	private Player lastWitherBlockPlacer;

	@EventHandler
	public void onEntityExplode(final EntityExplodeEvent event){
		if(!canEntityExplodeBlocks(event.getEntity(), event.blockList())){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntitySpawn(final EntitySpawnEvent event){
		if(event.getEntity() instanceof Wither){
			witherSummoners.put(event.getEntity().getUniqueId(), lastWitherBlockPlacer);
		}
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event){
		Material blockMaterial = event.getBlockPlaced().getType();
		if(blockMaterial.equals(Material.SOUL_SAND) || blockMaterial.equals(Material.WITHER_SKELETON_SKULL)){
			lastWitherBlockPlacer = event.getPlayer();
		}
	}

	private boolean canEntityExplodeBlocks(Entity entity, List<Block> blockList) {
		for (final Block block:blockList) {
			if(!canEntityExplodeBlock(entity, block)){
				return false;
			}
		}
		return true;
	}

	private boolean canEntityExplodeBlock(Entity entity, Block block) {
		if(entity instanceof Wither wither){
			return canWitherExplodeBlock(wither, block);
		} else if(entity instanceof WitherSkull witherSkull){
			return canWitherSkullExplodeBlock(witherSkull, block);
		} else if(entity instanceof TNTPrimed tntPrimed){
			return canTNTPrimedExplodeBlock(tntPrimed, block);
		} else if(entity instanceof ExplosiveMinecart explosiveMinecart){
			return canExplosiveMinecraftExplodeBlock(explosiveMinecart, block);
		} else if(entity instanceof EnderCrystal enderCrystal){
			return canEnderCrystalExplodeBlock(enderCrystal, block);
		} else {
			return false;
		}
	}

	private boolean canWitherExplodeBlock(Wither wither, Block block) {
		Plot blockPlot = PlotManager.getInstance().getPlot(block.getChunk());
		UUID uuid = wither.getUniqueId();
		Player summoner = witherSummoners.get(uuid);

		if(summoner != null){
			return PlotPermissionUtil.canPlayerAffectPlot(summoner, blockPlot);
		} else {
			return false;
		}
	}

	private boolean canWitherSkullExplodeBlock(WitherSkull witherSkull, Block block) {
		if(witherSkull.getShooter() instanceof Wither wither) {
			return canWitherExplodeBlock(wither, block);
		}
		return false;
	}

	private boolean canTNTPrimedExplodeBlock(TNTPrimed tntPrimed, Block block) {
		Entity tntSource = tntPrimed.getSource();
		if(tntSource != null) {
			if (tntSource.isValid()) {
				Plot blockPlot = PlotManager.getInstance().getPlot(block.getChunk());
				if (tntSource instanceof Player player) {
					return PlotPermissionUtil.canPlayerAffectPlot(player, blockPlot);
				}
			}
		}
		return false;
	}

	private boolean canExplosiveMinecraftExplodeBlock(ExplosiveMinecart explosiveMinecart, Block block) {
		//TODO: Check if exploding tntminecart is the same thing as tntprimed and if it isn't handle it here
		return false;
	}

	private boolean canEnderCrystalExplodeBlock(EnderCrystal enderCrystal, Block block) {
		Plot blockPlot = PlotManager.getInstance().getPlot(block.getChunk());
		Plot crystalPlot = PlotManager.getInstance().getPlot(enderCrystal.getLocation().getChunk());
		return blockPlot.hasTheSameOwnerAs(crystalPlot);
	}
}
