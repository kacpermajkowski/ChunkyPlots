package pl.kacpermajkowski.ChunkyPlots.messages;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.user.User;

import java.util.ArrayList;
import java.util.List;

public class TextUtil {
    public static String fixColors(String string){
        return ChatColor.translateAlternateColorCodes('&', string);
    }
    public static List<String> fixColors(List<String> list){
        ArrayList<String> fixed = new ArrayList<>();
        for(String string: list)
            fixed.add(ChatColor.translateAlternateColorCodes('&', string));
        return fixed;
    }

    public static void sendActionBarMessage(Player player, String message){
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                new TextComponent(message)
        );
    }

    private static BossBar bossBar;
    private static BukkitTask task = null;
    public static void sendBossBarMessage(Player player, String message){
        //TODO: Add configurable boss bars or at least a message-type dependent loo
        if(bossBar == null){
            bossBar = Bukkit.createBossBar(message, BarColor.YELLOW, BarStyle.SEGMENTED_10);
            bossBar.setProgress(1.0);
            bossBar.setVisible(true);
        } else {
            task.cancel();
            bossBar.setTitle(message);
        }

        bossBar.addPlayer(player);

        task = Bukkit.getScheduler().runTaskLater(ChunkyPlots.getInstance(), () -> {
            bossBar.removePlayer(player);
        }, 60);
    }

    @Deprecated
    public static void sendNoPrefixMessage(CommandSender sender, String s) {
        sender.sendMessage(fixColors(s));
    }

    @Deprecated
    public static void sendMessage(CommandSender sender, String s) {
        sender.sendMessage(fixColors(Config.getInstance().getPrefix() + Config.getInstance().getPrefixSpacer() + s));
    }

    @Deprecated
    public static String replacePlaceholders(String message, Player player){
        message = message.replace("{user}", player.getName());
        return message;
    }
    @Deprecated
    public static String replacePlaceholders(String message, User user){
        message = message.replace("{user}", user.getName());
        return message;
    }
    @Deprecated
    public static String replacePlaceholders(String message, Plot plot){
        message = message.replace("{plotID}", plot.getID());
        message = message.replace("{world}", plot.getWorldName());
        return message;
    }
    @Deprecated
    public static String replacePlaceholders(String message, Plot plot, Player player){
        message = replacePlaceholders(message, plot);
        message = replacePlaceholders(message, player);
        return message;
    }
    @Deprecated
    public static String replacePlaceholders(String message, Plot plot, User user){
        message = replacePlaceholders(message, plot);
        message = replacePlaceholders(message, user);
        return message;
    }

}
