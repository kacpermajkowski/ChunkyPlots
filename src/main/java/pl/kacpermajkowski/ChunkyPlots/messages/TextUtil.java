package pl.kacpermajkowski.ChunkyPlots.messages;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();

    private static final Pattern LEGACY_AMPERSAND_PATTERN = Pattern.compile("&([0-9a-fk-orA-FK-OR])");

    public static Component toComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        if (text.contains("<") && text.contains(">")) {
            try {
                String converted = convertLegacyAmpersandToMiniMessage(text);
                return MINI_MESSAGE.deserialize(converted);
            } catch (Exception ignored) {
                // Fallback to legacy parser if MiniMessage fails
            }
        }

        if (text.contains("&")) {
            return AMPERSAND_SERIALIZER.deserialize(text);
        }
        return SECTION_SERIALIZER.deserialize(text);
    }

    private static String convertLegacyAmpersandToMiniMessage(String text) {
        return LEGACY_AMPERSAND_PATTERN.matcher(text).replaceAll(mr -> {
            char code = Character.toLowerCase(mr.group(1).charAt(0));
            return switch (code) {
                case '0' -> "<black>";
                case '1' -> "<dark_blue>";
                case '2' -> "<dark_green>";
                case '3' -> "<dark_aqua>";
                case '4' -> "<dark_red>";
                case '5' -> "<dark_purple>";
                case '6' -> "<gold>";
                case '7' -> "<gray>";
                case '8' -> "<dark_gray>";
                case '9' -> "<blue>";
                case 'a' -> "<green>";
                case 'b' -> "<aqua>";
                case 'c' -> "<red>";
                case 'd' -> "<light_purple>";
                case 'e' -> "<yellow>";
                case 'f' -> "<white>";
                case 'k' -> "<obfuscated>";
                case 'l' -> "<bold>";
                case 'm' -> "<strikethrough>";
                case 'n' -> "<underlined>";
                case 'o' -> "<italic>";
                case 'r' -> "<reset>";
                default -> mr.group(0);
            };
        });
    }

    public static String fixColors(String string) {
        if (string == null) return "";
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> fixColors(List<String> list) {
        if (list == null) return List.of();
        ArrayList<String> fixed = new ArrayList<>();
        for (String string : list) {
            fixed.add(ChatColor.translateAlternateColorCodes('&', string));
        }
        return fixed;
    }

    public static void sendActionBarMessage(Player player, String message) {
        if (player == null || message == null) return;
        player.sendActionBar(toComponent(message));
    }

    public static void sendActionBar(Player player, Component component) {
        if (player == null || component == null) return;
        player.sendActionBar(component);
    }

    public static void sendBossBarMessage(Player player, String message) {
        sendBossBarMessage(player, toComponent(message));
    }

    public static void sendBossBarMessage(Player player, Component component) {
        if (player == null || component == null) return;
        BossBar bar = BossBar.bossBar(component, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_10);
        player.showBossBar(bar);

        Plugin plugin = null;
        try {
            plugin = ChunkyPlots.instance();
        } catch (Exception ignored) {}

        if (plugin != null && plugin.isEnabled()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.hideBossBar(bar);
                }
            }, 60L);
        }
    }

    @Deprecated
    public static void sendNoPrefixMessage(CommandSender sender, String s) {
        if (sender != null && s != null) {
            sender.sendMessage(toComponent(s));
        }
    }

    @Deprecated
    public static void sendMessage(CommandSender sender, String s) {
        if (sender != null && s != null) {
            sender.sendMessage(toComponent(s));
        }
    }

    @Deprecated
    public static String replacePlaceholders(String message, Player player) {
        if (message == null || player == null) return message;
        return message.replace("{user}", player.getName());
    }

    @Deprecated
    public static String replacePlaceholders(String message, User user) {
        if (message == null || user == null) return message;
        return message.replace("{user}", user.getName());
    }

    @Deprecated
    public static String replacePlaceholders(String message, Plot plot) {
        if (message == null || plot == null) return message;
        message = message.replace("{plotID}", plot.getID());
        message = message.replace("{world}", plot.getWorldName());
        return message;
    }

    @Deprecated
    public static String replacePlaceholders(String message, Plot plot, Player player) {
        message = replacePlaceholders(message, plot);
        return replacePlaceholders(message, player);
    }

    @Deprecated
    public static String replacePlaceholders(String message, Plot plot, User user) {
        message = replacePlaceholders(message, plot);
        return replacePlaceholders(message, user);
    }
}
