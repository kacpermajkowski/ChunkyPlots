package pl.kacpermajkowski.ChunkyPlots.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.events.PlotDisposeEvent;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;
import pl.kacpermajkowski.ChunkyPlots.utils.PlayerUtil;

public class PlotLifecycleListener implements Listener {
    private final UserManager userManager;

    public PlotLifecycleListener(UserManager userManager) {
        this.userManager = userManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlotDispose(PlotDisposeEvent event) {
        Plot disposedPlot = event.getPlot();
        MessageBuilder mb = new MessageBuilder(Message.CURRENT_PLOT_DELETED).plot(disposedPlot);
        PlayerUtil.getPlayersInPlot(disposedPlot).forEach(mb::sendAll);

        for (User user : userManager.getUsers()) {
            if (disposedPlot.equals(user.getCachedCurrentPlot())) {
                user.setCachedCurrentPlot(null);
            }
        }
    }
}
