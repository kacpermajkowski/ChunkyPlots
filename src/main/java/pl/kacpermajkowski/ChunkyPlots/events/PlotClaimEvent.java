package pl.kacpermajkowski.ChunkyPlots.events;

import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;

public class PlotClaimEvent extends PlotEvent {
    private final Player player;

    public PlotClaimEvent(Plot plot, Player player) {
        super(plot);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
