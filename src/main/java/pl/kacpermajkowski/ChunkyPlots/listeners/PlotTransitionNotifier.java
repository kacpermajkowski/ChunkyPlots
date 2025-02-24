package pl.kacpermajkowski.ChunkyPlots.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.basic.User;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.manager.UserManager;
import pl.kacpermajkowski.ChunkyPlots.util.MessageBuilder;

public class PlotTransitionNotifier implements Listener {
    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Location to = event.getTo();
        if (to == null) return;

        final Player player = event.getPlayer();
        final User user = UserManager.getInstance().getUser(player);
        if(user == null) return; //TODO: should probably throw some kind of illegal state exception

        final Plot fromPlot = PlotManager.getInstance().getPlot(event.getFrom());
        final Plot toPlot = PlotManager.getInstance().getPlot(to);

        if(fromPlot == toPlot) return;
        else if(toPlot == null) handleLeavingPlot(player, fromPlot);
        else if(fromPlot == null) handleEnteringPlot(player, toPlot);
        else handleSwitchingPlots(player, fromPlot, toPlot);
    }

    private void handleLeavingPlot(Player player, Plot fromPlot) {
        if(!fromPlot.isPlayerBlacklisted(player))
        sendLeaveMessage(player, fromPlot);
    }

    private void handleEnteringPlot(Player player, Plot toPlot) {
        if(!toPlot.isPlayerBlacklisted(player))
            sendEntryMessage(player, toPlot);
    }

    private void handleSwitchingPlots(Player player, Plot fromPlot, Plot toPlot) {
        if(!fromPlot.hasTheSameOwnerAs(toPlot)){
            handleEnteringPlot(player, toPlot);
        }
    }

    private void sendEntryMessage(Player player, Plot newPlot){
        new MessageBuilder(Message.ENTERED_PLOT).plot(newPlot).send(player);
    }
    private void sendLeaveMessage(Player player, Plot previousPlot){
        new MessageBuilder(Message.LEFT_PLOT).plot(previousPlot).send(player);
    }
}
