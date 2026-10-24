package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class BlockPlaceListener implements Listener {
    private final PlotPermissionChecker checker;

    public BlockPlaceListener(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent event){
        final Player player = event.getPlayer();
        final Plot eventPlot = checker.getPlot(event.getBlockPlaced());

        if(checker.canPlayerAffect(player, eventPlot)) return;
        event.setCancelled(true);
        ProtectionNotifier.notify(player, Message.PROTECTION_BLOCK_PLACE, eventPlot);
    }
}
