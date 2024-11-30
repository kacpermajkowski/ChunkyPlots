package pl.kacpermajkowski.ChunkyPlots.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.MessageType;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.basic.User;
import pl.kacpermajkowski.ChunkyPlots.manager.ConfigManager;
import pl.kacpermajkowski.ChunkyPlots.manager.MessageManager;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.manager.UserManager;

public class PlayerMoveListener implements Listener {
    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (to != null && from.distance(to) != 0){
            final PlotManager plotManager = ChunkyPlots.plugin.plotManager;
            final UserManager userManager = ChunkyPlots.plugin.userManager;
            final ConfigManager configManager = ChunkyPlots.plugin.configManager;

            final Player player = event.getPlayer();
            final User user = userManager.getUser(player.getName());

            final Plot previousPlot = plotManager.getPlotByChunk(from.getChunk());
            final Plot newPlot = plotManager.getPlotByChunk(to.getChunk());

            if(user != null) {
                if(newPlot != null && newPlot.blacklist.contains(user.getNickname())) event.setCancelled(true);
                if (!user.hasEntered) {
                    if(newPlot != null) {
                        if(!newPlot.blacklist.contains(player.getName())) {
                            sendEnterMessages(player, configManager, newPlot);
                            user.hasEntered = true;
                        } else event.setCancelled(true);
                    }
                } else {
                    if(previousPlot != null) {
                        if (newPlot != null) {
                            if (!newPlot.equals(previousPlot)) {
                                if (!newPlot.getOwnerNickname().equals(previousPlot.getOwnerNickname())) {
                                    sendEnterMessages(player, configManager, newPlot);
                                    user.hasEntered = true;
                                }
                            }
                        } else {
                            sendLeaveMessages(player, configManager, previousPlot);
                            user.hasEntered = false;
                        }
                    } else {
                        if(newPlot == null){
                            MessageManager.sendMessage(player, "&cDziałka, na której stałeś została usunięta!");
                            user.hasEntered = false;
                        } else {
                            if (!newPlot.getOwnerNickname().equals(previousPlot.getOwnerNickname())) {
                                sendEnterMessages(player, configManager, newPlot);
                                user.hasEntered = true;
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendEnterMessages(Player player, ConfigManager configManager, Plot newPlot){
        MessageManager.sendMessage(player, configManager.getMessage(MessageType.ENTERED_PLOT).replace("{plotOwnerName}", newPlot.getOwnerNickname()));
    }
    private void sendLeaveMessages(Player player, ConfigManager configManager, Plot previousPlot){
        MessageManager.sendMessage(player, configManager.getMessage(MessageType.LEFT_PLOT).replace("{plotOwnerName}", previousPlot.getOwnerNickname()));
    }
}
