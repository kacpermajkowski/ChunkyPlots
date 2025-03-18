package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class BlockPlaceListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent event){
        final Block block = event.getBlock();
        final Player player = event.getPlayer();
        final Plot eventPlot = PlotManager.getInstance().getPlot(event.getBlockPlaced());

        event.setCancelled(!ProtectionUtil.canPlayerAffectPlot(player, eventPlot));
    }
}
