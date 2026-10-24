package pl.kacpermajkowski.ChunkyPlots.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;

public class PlotTransitionNotifier implements Listener {
    private final PlotManager plotManager;
    private final UserManager userManager;

    public PlotTransitionNotifier(PlotManager plotManager, UserManager userManager) {
        this.plotManager = plotManager;
        this.userManager = userManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();

        // idk precisely when it happens, but the getter is marked as nullable
        if (to == null) return;

        User user = userManager.getUser(event.getPlayer());
        if(user == null)
            throw new IllegalStateException("There is no User mapped to Player.");

        Plot fromPlot = user.getCachedCurrentPlot();
        Plot toPlot = plotManager.getPlot(to);

        if(toPlot == null && fromPlot != null)
            handleLeavingPlot(user, fromPlot);
        else if(fromPlot == null && toPlot != null)
            handleEnteringPlot(user, toPlot);
        else if(fromPlot != null && toPlot != null)
            handleSwitchingPlots(user, fromPlot, toPlot);
    }

    private void handleLeavingPlot(User user, Plot fromPlot) {
        if(!fromPlot.isPlayerBlacklisted(user)) {
            sendLeaveMessage(user, fromPlot);
            user.setCachedCurrentPlot(null);
        }
    }

    private void handleEnteringPlot(User user, Plot toPlot) {
        if(!toPlot.isPlayerBlacklisted(user)) {
            sendEntryMessage(user, toPlot);
            user.setCachedCurrentPlot(toPlot);
        }
    }

    private void handleSwitchingPlots(User user, Plot fromPlot, Plot toPlot) {
        if(!fromPlot.hasTheSameOwnerAs(toPlot)){
            handleEnteringPlot(user, toPlot);
        }
    }

    private void sendEntryMessage(User user, Plot newPlot){
        new MessageBuilder(Message.ENTERED_PLOT).plot(newPlot).sendAll(user.getPlayer());
    }
    private void sendLeaveMessage(User user, Plot previousPlot){
        new MessageBuilder(Message.LEFT_PLOT).plot(previousPlot).sendAll(user.getPlayer());
    }
}
