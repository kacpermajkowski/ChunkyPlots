package pl.kacpermajkowski.ChunkyPlots.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.basic.*;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.config.lang.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;

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

    @Deprecated
    public static void sendNoPrefixMessage(CommandSender sender, String s) {
        sender.sendMessage(fixColors(s));
    }

    @Deprecated
    public static void sendMessage(CommandSender sender, String s) {
        sender.sendMessage(fixColors(Config.getInstance().getPrefix() + Config.getInstance().getPrefixSpacer() + s));
    }
    @Deprecated
    public static String replacePlaceholders(String message, VisitPoint visitPoint){
        message = message.replace("{visitPointName}", visitPoint.getName());
        return message;
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
    @Deprecated
    public static String replacePlaceholders(String message, VisitPoint visitPoint, Player player){
        message = replacePlaceholders(message, player);
        message = replacePlaceholders(message, visitPoint);
        return message;
    }

}
