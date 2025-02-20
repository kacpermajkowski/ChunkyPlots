package pl.kacpermajkowski.ChunkyPlots.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.User;
import pl.kacpermajkowski.ChunkyPlots.manager.UserManager;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event){
        final Player player = event.getPlayer();

        final UserManager userManager = UserManager.getInstance();
        User user = userManager.getUser(player.getName());
        if(user == null){
            user = new User(event.getPlayer().getName());
            userManager.saveUser(user);
        }
        userManager.addUser(user);
    }
}
