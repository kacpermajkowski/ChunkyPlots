package pl.kacpermajkowski.ChunkyPlots.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.basic.User;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.manager.UserManager;
import pl.kacpermajkowski.ChunkyPlots.util.TextUtil;

public class PlayerMoveListener implements Listener {
    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (to != null && from.distance(to) != 0){
            final PlotManager plotManager = PlotManager.getInstance();
            final UserManager userManager = UserManager.getInstance();

            final Player player = event.getPlayer();
            final User user = userManager.getUser(player.getName());

            final Plot previousPlot = plotManager.getPlotByChunk(from.getChunk());
            final Plot newPlot = plotManager.getPlotByChunk(to.getChunk());

            if(user != null) {
                if(newPlot != null && newPlot.blacklist.contains(user.getNickname())) event.setCancelled(true);
                if (!user.hasEntered) {
                    if(newPlot != null) {
                        if(!newPlot.blacklist.contains(player.getName())) {
                            sendEnterMessages(player, newPlot);
                            user.hasEntered = true;
                        } else event.setCancelled(true);
                    }
                } else {
                    if(previousPlot != null) {
                        if (newPlot != null) {
                            if (!newPlot.equals(previousPlot)) {
                                if (!newPlot.getOwnerNickname().equals(previousPlot.getOwnerNickname())) {
                                    sendEnterMessages(player, newPlot);
                                    user.hasEntered = true;
                                }
                            }
                        } else {
                            sendLeaveMessages(player, previousPlot);
                            user.hasEntered = false;
                        }
                    } else {
                        if(newPlot == null){
                            TextUtil.sendMessage(player, "&cDziałka, na której stałeś została usunięta!");
                            user.hasEntered = false;
                        } else {
                            if (!newPlot.getOwnerNickname().equals(previousPlot.getOwnerNickname())) {
                                sendEnterMessages(player, newPlot);
                                user.hasEntered = true;
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendEnterMessages(Player player, Plot newPlot){
        TextUtil.sendMessage(player, Config.getInstance().getMessage(Message.ENTERED_PLOT).replace("{plotOwnerName}", newPlot.getOwnerNickname()));
    }
    private void sendLeaveMessages(Player player, Plot previousPlot){
        TextUtil.sendMessage(player, Config.getInstance().getMessage(Message.LEFT_PLOT).replace("{plotOwnerName}", previousPlot.getOwnerNickname()));
    }
}
