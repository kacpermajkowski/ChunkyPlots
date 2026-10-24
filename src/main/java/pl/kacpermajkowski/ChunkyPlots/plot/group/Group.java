package pl.kacpermajkowski.ChunkyPlots.plot.group;

import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.OfflinePlayer;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;

import java.util.*;
import java.util.function.Supplier;

public class Group {
    public enum GroupFlagStatus {
        ALL_TRUE,
        ALL_FALSE,
        DIFFERENT,
        EMPTY
    }

    private final List<Plot> plots = new ArrayList<>();
    @Getter
    private final String name;
    @Getter
    private final boolean isDefault;
    @Getter
    private final UUID ownerUuid;
    private Supplier<List<Plot>> allPlotsSupplier;

    public Group() {
        this("all", null, true);
    }

    public Group(String name) {
        this(name, null, "all".equalsIgnoreCase(name));
    }

    public Group(String name, UUID ownerUuid, boolean isDefault) {
        this.name = name != null ? name : "all";
        this.ownerUuid = ownerUuid;
        this.isDefault = isDefault || "all".equalsIgnoreCase(this.name);
    }

    public void setAllPlotsSupplier(Supplier<List<Plot>> allPlotsSupplier) {
        this.allPlotsSupplier = allPlotsSupplier;
    }

    public List<Plot> getPlots() {
        if (isDefault && allPlotsSupplier != null) {
            List<Plot> dynamicPlots = allPlotsSupplier.get();
            if (dynamicPlots != null) {
                return Collections.unmodifiableList(dynamicPlots);
            }
        }
        return Collections.unmodifiableList(plots);
    }

    public void add(Plot plot) {
        if (plot == null) return;
        if (!contains(plot)) {
            plots.add(plot);
        }
    }

    public void remove(Plot plot) {
        if (plot == null) return;
        plots.remove(plot);
    }

    public boolean contains(Plot plot) {
        if (plot == null) return false;
        return contains(plot.getUUID());
    }

    public boolean contains(UUID plotUUID) {
        if (plotUUID == null) return false;
        for (Plot plot : getPlots()) {
            if (plot != null && plotUUID.equals(plot.getUUID())) {
                return true;
            }
        }
        return false;
    }

    public GroupFlagStatus getFlagStatus(PlotFlag flag) {
        List<Plot> plotList = getPlots();
        if (plotList.isEmpty()) return GroupFlagStatus.EMPTY;

        boolean first = plotList.get(0).getFlag(flag);
        for (int i = 1; i < plotList.size(); i++) {
            if (plotList.get(i).getFlag(flag) != first) {
                return GroupFlagStatus.DIFFERENT;
            }
        }
        return first ? GroupFlagStatus.ALL_TRUE : GroupFlagStatus.ALL_FALSE;
    }

    public void setFlag(PlotFlag flag, boolean value) {
        if (flag == null) return;
        for (Plot plot : getPlots()) {
            if (plot != null) {
                plot.setFlag(flag, value);
            }
        }
    }

    public void resetFlag(PlotFlag flag) {
        if (flag == null) return;
        for (Plot plot : getPlots()) {
            if (plot != null) {
                plot.resetFlag(flag);
            }
        }
    }

    public void resetAllFlags() {
        for (Plot plot : getPlots()) {
            if (plot != null) {
                plot.resetAllFlags();
            }
        }
    }

    public void whitelistPlayer(UUID playerUuid) {
        if (playerUuid == null) return;
        for (Plot plot : getPlots()) {
            if (plot != null) {
                if (plot.isPlayerBlacklisted(playerUuid)) {
                    plot.unblacklistPlayer(playerUuid);
                }
                plot.whitelistPlayer(playerUuid);
            }
        }
    }

    public void whitelistPlayer(OfflinePlayer player) {
        if (player != null) whitelistPlayer(player.getUniqueId());
    }

    public void unwhitelistPlayer(UUID playerUuid) {
        if (playerUuid == null) return;
        for (Plot plot : getPlots()) {
            if (plot != null) {
                plot.unwhitelistPlayer(playerUuid);
            }
        }
    }

    public void unwhitelistPlayer(OfflinePlayer player) {
        if (player != null) unwhitelistPlayer(player.getUniqueId());
    }

    public void blacklistPlayer(UUID playerUuid) {
        if (playerUuid == null) return;
        for (Plot plot : getPlots()) {
            if (plot != null) {
                if (plot.isPlayerWhitelisted(playerUuid)) {
                    plot.unwhitelistPlayer(playerUuid);
                }
                plot.blacklistPlayer(playerUuid);
            }
        }
    }

    public void blacklistPlayer(OfflinePlayer player) {
        if (player != null) blacklistPlayer(player.getUniqueId());
    }

    public void unblacklistPlayer(UUID playerUuid) {
        if (playerUuid == null) return;
        for (Plot plot : getPlots()) {
            if (plot != null) {
                plot.unblacklistPlayer(playerUuid);
            }
        }
    }

    public void unblacklistPlayer(OfflinePlayer player) {
        if (player != null) unblacklistPlayer(player.getUniqueId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group group)) return false;

        return new EqualsBuilder()
                .append(name.toLowerCase(), group.name.toLowerCase())
                .append(ownerUuid, group.ownerUuid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name != null ? name.toLowerCase() : "")
                .append(ownerUuid)
                .toHashCode();
    }
}
