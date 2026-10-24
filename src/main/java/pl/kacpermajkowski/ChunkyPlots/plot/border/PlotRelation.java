package pl.kacpermajkowski.ChunkyPlots.plot.border;

import org.bukkit.Color;

public enum PlotRelation {
    WILDERNESS(Color.fromRGB(255, 215, 0), "&e", "Dzicz", "Wilderness"),
    OWN_PLOT(Color.fromRGB(50, 255, 50), "&a", "Twoja działka", "Own Plot"),
    MEMBER_PLOT(Color.fromRGB(0, 200, 255), "&b", "Działka członka", "Member Plot"),
    THIRD_PARTY_PLOT(Color.fromRGB(255, 50, 50), "&c", "Obca działka", "Third-Party Plot");

    private final Color color;
    private final String chatColor;
    private final String plName;
    private final String enName;

    PlotRelation(Color color, String chatColor, String plName, String enName) {
        this.color = color;
        this.chatColor = chatColor;
        this.plName = plName;
        this.enName = enName;
    }

    public Color getColor() {
        return color;
    }

    public String getChatColor() {
        return chatColor;
    }

    public String getPlName() {
        return plName;
    }

    public String getEnName() {
        return enName;
    }
}
