package pl.kacpermajkowski.ChunkyPlots.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

public class EventUtils {

    /// Checks whether event.getPlayer() can affect the plot, and sets event.setCancelled(true) if not.
    ///
    /// @return true if event was cancelled, false otherwise
    public static <T extends PlayerEvent & Cancellable> boolean cancelEventIfPlayerNotPermitted(T event, Plot plot) {
        return cancelEventIfPlayerNotPermitted(event, event.getPlayer(), plot);
    }

    /// Checks whether the player argument can affect the plot, and sets event.setCancelled(true) if not.
    ///
    /// @return true if event was cancelled, false otherwise
    public static boolean cancelEventIfPlayerNotPermitted(Cancellable event, Player player, Plot plot) {
        if(!ProtectionUtil.canPlayerAffect(player, plot)) {
            event.setCancelled(true);
            return true;
        }
        return false;
    }

}
