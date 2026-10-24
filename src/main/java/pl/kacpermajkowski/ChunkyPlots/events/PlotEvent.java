package pl.kacpermajkowski.ChunkyPlots.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;

public abstract class PlotEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    protected final Plot plot;

    public PlotEvent(Plot plot) {
        this.plot = plot;
    }

    public PlotEvent(Plot plot, boolean isAsync) {
        super(isAsync);
        this.plot = plot;
    }

    public Plot getPlot() {
        return plot;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
