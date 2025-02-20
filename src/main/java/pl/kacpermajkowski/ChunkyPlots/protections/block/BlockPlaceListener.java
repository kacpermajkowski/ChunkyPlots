package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import pl.kacpermajkowski.ChunkyPlots.basic.Flag;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlotPermissionUtil;
import pl.kacpermajkowski.ChunkyPlots.util.TextUtil;

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
                TextUtil.sendMessage(player, message);
            } else {
                if(isBlockAPlotBlock(event.getItemInHand())){
                    event.setCancelled(true);
                    String message = Config.getInstance().getMessage(Message.PLOT_ALREADY_EXISTS);
                    TextUtil.sendMessage(player, message);
                }
            }
        } else {
            if(shouldPlotBeCreated(event)){
                if(!hasBlockBeenPlacedInRestrictedArea(block)) {
                    PlotManager.getInstance().claimPlot(player, block);
                    block.setType(Material.AIR);
                } else {
                    String message = Config.getInstance().getMessage(Message.NOT_PERMITTED);
                    TextUtil.sendMessage(player, message);
                }
            }
        }
    }

    private boolean shouldPlotBeCreated(BlockPlaceEvent event) {
        if(!event.isCancelled()){
            Player player = event.getPlayer();
            Block block = event.getBlockPlaced();
			return isBlockAPlotBlock(event.getItemInHand());
        }
        return false;
    }
    private boolean isBlockAPlotBlock(ItemStack block) {
        return block.isSimilar(PlotManager.getInstance().getPlotItem());
    }

    private boolean hasBlockBeenPlacedInRestrictedArea(Block block) {
		return block.getWorld().getName().equals("world_the_end");
	}

}
