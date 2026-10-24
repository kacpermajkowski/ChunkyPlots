package pl.kacpermajkowski.ChunkyPlots.plot.limit;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.hook.vault.VaultHook;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

import java.util.Map;
import java.util.UUID;

/**
 * Calculates and manages permission-based and Vault group-based plot count limits for players.
 */
public class PlotLimitManager {
    public static final String PERMISSION_LIMIT_PREFIX = "chunkyplots.limit.";
    public static final String PERMISSION_UNLIMITED = "chunkyplots.limit.unlimited";
    public static final String PERMISSION_WILDCARD = "chunkyplots.limit.*";
    public static final String PERMISSION_ADMIN = "chunkyplots.admin";
    public static final String PERMISSION_BYPASS = "chunkyplots.plotadmin";

    private final Config config;
    private final PlotManager plotManager;
    private final VaultHook vaultHook;

    public PlotLimitManager(Config config, PlotManager plotManager, VaultHook vaultHook) {
        this.config = config;
        this.plotManager = plotManager;
        this.vaultHook = vaultHook;
    }

    /**
     * Resolves the maximum number of plots a player is permitted to own.
     * Evaluates permission nodes (e.g. chunkyplots.limit.5), Vault groups, and config defaults.
     *
     * @param player Target player
     * @return Maximum plots allowed, or Integer.MAX_VALUE if unlimited
     */
    public int getPlotLimit(Player player) {
        if (player == null) {
            return getDefaultLimit();
        }

        // 1. Unlimited permissions check
        if (player.isOp() ||
                player.hasPermission(PERMISSION_UNLIMITED) ||
                player.hasPermission(PERMISSION_WILDCARD) ||
                player.hasPermission(PERMISSION_ADMIN) ||
                player.hasPermission(PERMISSION_BYPASS)) {
            return Integer.MAX_VALUE;
        }

        int maxPermissionLimit = 0;

        // 2. Scan permission attachments for chunkyplots.limit.<number>
        if (player.getEffectivePermissions() != null) {
            for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
                if (pai == null || !pai.getValue() || pai.getPermission() == null) continue;

                String perm = pai.getPermission().toLowerCase();
                if (perm.startsWith(PERMISSION_LIMIT_PREFIX)) {
                    String suffix = perm.substring(PERMISSION_LIMIT_PREFIX.length());
                    if (suffix.equals("unlimited") || suffix.equals("*")) {
                        return Integer.MAX_VALUE;
                    }
                    try {
                        int parsed = Integer.parseInt(suffix);
                        if (parsed > maxPermissionLimit) {
                            maxPermissionLimit = parsed;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // 3. Vault group-based limits
        int maxGroupLimit = 0;
        if (vaultHook != null && vaultHook.isHooked() && config != null) {
            Map<String, Integer> groupLimits = config.getGroupPlotLimits();
            if (groupLimits != null && !groupLimits.isEmpty()) {
                String primaryGroup = vaultHook.getPrimaryGroup(player);
                if (primaryGroup != null) {
                    Integer limit = groupLimits.get(primaryGroup.toLowerCase());
                    if (limit != null && limit > maxGroupLimit) {
                        maxGroupLimit = limit;
                    }
                }
                String[] playerGroups = vaultHook.getPlayerGroups(player);
                if (playerGroups != null) {
                    for (String group : playerGroups) {
                        if (group != null) {
                            Integer limit = groupLimits.get(group.toLowerCase());
                            if (limit != null && limit > maxGroupLimit) {
                                maxGroupLimit = limit;
                            }
                        }
                    }
                }
            }
        }

        int resolved = Math.max(maxPermissionLimit, maxGroupLimit);
        return resolved > 0 ? resolved : getDefaultLimit();
    }

    /**
     * Gets the count of plots owned by player.
     */
    public int getPlotCount(Player player) {
        if (player == null) return 0;
        return getPlotCount(player.getUniqueId());
    }

    /**
     * Gets the count of plots owned by player UUID.
     */
    public int getPlotCount(UUID playerUuid) {
        if (playerUuid == null || plotManager == null) return 0;
        return plotManager.getPlotsByOwner(playerUuid).size();
    }

    /**
     * Gets remaining available plots a player can claim.
     */
    public int getRemainingPlots(Player player) {
        int limit = getPlotLimit(player);
        if (limit == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        int current = getPlotCount(player);
        return Math.max(0, limit - current);
    }

    /**
     * Checks if player has reached or exceeded their allowed plot limit.
     */
    public boolean hasReachedPlotLimit(Player player) {
        int limit = getPlotLimit(player);
        if (limit == Integer.MAX_VALUE) {
            return false;
        }
        return getPlotCount(player) >= limit;
    }

    /**
     * Checks if player can claim at least one more plot.
     */
    public boolean canClaimMorePlots(Player player) {
        return !hasReachedPlotLimit(player);
    }

    private int getDefaultLimit() {
        return config != null ? config.getDefaultPlotLimit() : 1;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }
}
