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
            final User user = userManager.getUser(player.getUniqueId());

            final Plot previousPlot = plotManager.getPlot(from.getChunk());
            final Plot newPlot = plotManager.getPlot(to.getChunk());

            if(user != null) {
                if(newPlot != null && newPlot.getBlacklist().contains(user.getName())) event.setCancelled(true);
                if (!user.isInsidePlot()) {
                    if(newPlot != null) {
                        if(!newPlot.getBlacklist().contains(player.getName())) {
                            sendEnterMessages(player, newPlot);
                            user.setInsidePlot(true);
                        } else event.setCancelled(true);
                    }
                } else {
                    if(previousPlot != null) {
                        if (newPlot != null) {
                            if (!newPlot.equals(previousPlot)) {
                                if (!newPlot.hasTheSameOwnerAs(previousPlot)) {
                                    sendEnterMessages(player, newPlot);
                                    user.setInsidePlot(true);
                                }
                            }
                        } else {
                            sendLeaveMessages(player, previousPlot);
                            user.setInsidePlot(false);
                        }
                    } else {
                        if(newPlot == null){
                            TextUtil.sendMessage(player, "&cDziałka, na której stałeś została usunięta!");
                            user.setInsidePlot(false);
                        } else {
                            if (!newPlot.hasTheSameOwnerAs(previousPlot)) {
                                sendEnterMessages(player, newPlot);
                                user.setInsidePlot(true);
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendEnterMessages(Player player, Plot newPlot){
        TextUtil.sendMessage(player, Config.getInstance().getMessage(Message.ENTERED_PLOT).replace("{plotOwnerName}", newPlot.getOwnerName()));
    }
    private void sendLeaveMessages(Player player, Plot previousPlot){
        TextUtil.sendMessage(player, Config.getInstance().getMessage(Message.LEFT_PLOT).replace("{plotOwnerName}", previousPlot.getOwnerName()));
    }
}
