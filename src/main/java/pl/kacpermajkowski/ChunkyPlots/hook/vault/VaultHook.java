package pl.kacpermajkowski.ChunkyPlots.hook.vault;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

/**
 * Service hook into Vault API.
 * Safely resolves Vault Permission and Chat service providers if present.
 */
public class VaultHook {
    private final Plugin plugin;
    private final Logger logger;
    private Permission permission;
    private Chat chat;
    private boolean hooked = false;

    public VaultHook(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin != null ? plugin.getLogger() : Logger.getLogger("ChunkyPlots");
        initialize();
    }

    public void initialize() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            if (logger != null) {
                logger.info("Vault not found. Defaulting to native Bukkit permissions.");
            }
            this.hooked = false;
            return;
        }

        try {
            RegisteredServiceProvider<Permission> rspPerm = Bukkit.getServicesManager().getRegistration(Permission.class);
            if (rspPerm != null) {
                this.permission = rspPerm.getProvider();
            }

            RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
            if (rspChat != null) {
                this.chat = rspChat.getProvider();
            }

            this.hooked = (this.permission != null || this.chat != null);
            if (this.hooked && logger != null) {
                logger.info("Successfully hooked into Vault (" +
                        (permission != null ? "Permission: " + permission.getName() : "") +
                        (chat != null ? ", Chat: " + chat.getName() : "") + ").");
            }
        } catch (Throwable t) {
            if (logger != null) {
                logger.warning("Could not initialize Vault hook: " + t.getMessage());
            }
            this.hooked = false;
        }
    }

    public boolean isHooked() {
        return hooked;
    }

    public boolean hasPermissionSupport() {
        return permission != null;
    }

    public boolean hasChatSupport() {
        return chat != null;
    }

    public Permission getPermission() {
        return permission;
    }

    public Chat getChat() {
        return chat;
    }

    /**
     * Gets primary permission group for player via Vault, or null if unsupported.
     */
    public String getPrimaryGroup(Player player) {
        if (player == null || permission == null) return null;
        try {
            if (permission.hasGroupSupport()) {
                return permission.getPrimaryGroup(player);
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /**
     * Gets all permission groups player belongs to.
     */
    public String[] getPlayerGroups(Player player) {
        if (player == null || permission == null) return new String[0];
        try {
            if (permission.hasGroupSupport()) {
                String[] groups = permission.getPlayerGroups(player);
                return groups != null ? groups : new String[0];
            }
        } catch (Throwable ignored) {}
        return new String[0];
    }

    /**
     * Checks if player is member of a specific permission group.
     */
    public boolean playerInGroup(Player player, String group) {
        if (player == null || group == null || permission == null) return false;
        try {
            if (permission.hasGroupSupport()) {
                return permission.playerInGroup(player, group);
            }
        } catch (Throwable ignored) {}
        return false;
    }

    /**
     * Checks a permission using Vault, falling back to Bukkit player.hasPermission.
     */
    public boolean playerHas(Player player, String perm) {
        if (player == null || perm == null) return false;
        if (permission != null) {
            try {
                return permission.has(player, perm);
            } catch (Throwable ignored) {}
        }
        return player.hasPermission(perm);
    }

    public String getPlayerPrefix(Player player) {
        if (player == null || chat == null) return "";
        try {
            String prefix = chat.getPlayerPrefix(player);
            return prefix != null ? prefix : "";
        } catch (Throwable ignored) {
            return "";
        }
    }

    public String getPlayerSuffix(Player player) {
        if (player == null || chat == null) return "";
        try {
            String suffix = chat.getPlayerSuffix(player);
            return suffix != null ? suffix : "";
        } catch (Throwable ignored) {
            return "";
        }
    }
}
