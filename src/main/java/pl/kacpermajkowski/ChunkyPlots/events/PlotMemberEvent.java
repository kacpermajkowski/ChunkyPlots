package pl.kacpermajkowski.ChunkyPlots.events;

import pl.kacpermajkowski.ChunkyPlots.plot.Plot;

import java.util.UUID;

public class PlotMemberEvent extends PlotEvent {
    public enum Action { ADD, REMOVE }
    public enum MemberType { WHITELIST, BLACKLIST }

    private final UUID memberUuid;
    private final Action action;
    private final MemberType memberType;

    public PlotMemberEvent(Plot plot, UUID memberUuid, Action action, MemberType memberType) {
        super(plot);
        this.memberUuid = memberUuid;
        this.action = action;
        this.memberType = memberType;
    }

    public UUID getMemberUuid() {
        return memberUuid;
    }

    public Action getAction() {
        return action;
    }

    public MemberType getMemberType() {
        return memberType;
    }
}
