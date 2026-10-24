package pl.kacpermajkowski.ChunkyPlots.protections.player;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.ChunkBorderUtil;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

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
        if (checker.canPlayerAffect(player, toPlot)) return;
        if (!toPlot.isPlayerBlacklisted(player)) return;

        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();
        int zDirection = fromChunk.getZ() - toChunk.getZ();
        int xDirection = fromChunk.getX() - toChunk.getX();

        if (zDirection == 0 && xDirection == 0) {
            Location closestBorderPoint = ChunkBorderUtil.getSafeBorderPoint(player, toChunk);
            closestBorderPoint.setYaw(event.getTo().getYaw());
            closestBorderPoint.setPitch(event.getTo().getPitch());

            player.teleport(closestBorderPoint);
        } else {
            Vector pushVector = new Vector(xDirection, event.getTo().getDirection().getY() + 0.2, zDirection);
            player.setVelocity(pushVector);
            new MessageBuilder(Message.CANNOT_ENTER_PLOT).username(toPlot.getOwnerName()).sendChat(player);
        }
    }
}
