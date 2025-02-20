package pl.kacpermajkowski.ChunkyPlots.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.User;
import pl.kacpermajkowski.ChunkyPlots.manager.UserManager;

public class PlayerLeftListener implements Listener {
    @EventHandler
    public void onPlayerLeft(final PlayerQuitEvent event){
        final Player player = event.getPlayer();

        final UserManager userManager = ChunkyPlots.getInstance().userManager;
        User user = userManager.getUser(player.getName());
        userManager.saveUser(user);
    }
}
