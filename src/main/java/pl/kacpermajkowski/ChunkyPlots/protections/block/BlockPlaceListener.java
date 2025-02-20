package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.PlayerInventory;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.Flag;
import pl.kacpermajkowski.ChunkyPlots.config.Message;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.manager.CraftingManager;
import pl.kacpermajkowski.ChunkyPlots.config.Lang;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;

public class BlockPlaceListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockPlaceEvent event){
        final Block block = event.getBlock();
        final Player player = event.getPlayer();
        final Plot blockPlot = PlotManager.getInstance().getPlotByChunk(block.getChunk());

        if(blockPlot != null){
            if(!PlotPermissionUtil.canPlayerAffectPlot(player, blockPlot, Flag.PLACE_MEMBER, Flag.PLACE_STRANGER)){
                event.setCancelled(true);
                String message = Config.getInstance().getMessage(Message.NOT_PERMITTED);
                Lang.sendMessage(player, message);
            } else {
                if(hasPlayerPlacedAPlotBlock(player, block)){
                    event.setCancelled(true);
                    String message = Config.getInstance().getMessage(Message.PLOT_ALREADY_EXISTS);
                    Lang.sendMessage(player, message);
                }
            }
        } else {
            if(shouldPlotBeCreated(event)){
                if(!hasBlockBeenPlacedInRestrictedArea(block)) {
                    PlotManager.getInstance().claimPlot(player, block);
                    block.setType(Material.AIR);
                } else {
                    String message = Config.getInstance().getMessage(Message.NOT_PERMITTED);
                    Lang.sendMessage(player, message);
                }
            }
        }
    }

    private boolean shouldPlotBeCreated(BlockPlaceEvent event) {
        if(!event.isCancelled()){
            Player player = event.getPlayer();
            Block block = event.getBlockPlaced();
			return hasPlayerPlacedAPlotBlock(player, block);
        }
        return false;
    }
    private boolean hasPlayerPlacedAPlotBlock(Player player, Block block) {
        PlayerInventory inventory = player.getInventory();
        if (inventory.getItemInMainHand().isSimilar(CraftingManager.plotBlock) || inventory.getItemInOffHand().isSimilar(CraftingManager.plotBlock)) {
			return block.getType().equals(Material.NOTE_BLOCK);
        }
        return false;
    }

    private boolean hasBlockBeenPlacedInRestrictedArea(Block block) {
		return block.getWorld().getName().equals("world_the_end");
	}

}
