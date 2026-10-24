package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.ChunkBorderUtil;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class PlayerEntryProtection implements Listener {
    private final PlotPermissionChecker checker;

    public PlayerEntryProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo() == null) return;
        if (event.getFrom().getWorld() == null || event.getTo().getWorld() == null) return;
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) return;
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }

        Plot toPlot = checker.getPlot(event.getTo());
        if (toPlot == null) return;
        if (!toPlot.getFlag(PlotFlag.ENTRY)) return;
        if (checker.canPlayerAffect(player, toPlot)) return;
        if (!toPlot.isPlayerBlacklisted(player)) return;

        Plot fromPlot = checker.getPlot(event.getFrom());
        boolean isFromSafe = (fromPlot == null || !fromPlot.isPlayerBlacklisted(player) || checker.canPlayerAffect(player, fromPlot))
                && !toPlot.equals(fromPlot);

        Location safeLocation;
        if (isFromSafe) {
            safeLocation = event.getFrom().clone();
        } else {
            safeLocation = ChunkBorderUtil.getSafeBorderPoint(player, toPlot.getChunkX(), toPlot.getChunkZ(), checker);
        }

        if (safeLocation != null) {
            safeLocation.setYaw(event.getTo().getYaw());
            safeLocation.setPitch(event.getTo().getPitch());
            player.teleport(safeLocation);
        }
        ProtectionNotifier.notify(player, Message.CANNOT_ENTER_PLOT, toPlot);
    }
}
