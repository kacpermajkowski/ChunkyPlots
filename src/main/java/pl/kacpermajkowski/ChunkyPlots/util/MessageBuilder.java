package pl.kacpermajkowski.ChunkyPlots.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.basic.*;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;

public class MessageBuilder {
    private Message message;

    private String plotID = "";
    private String plotOwnerName = "";
    private String worldName = "";

    private String userName = "";
    private String groupName = "";

    private String subcommandSyntax = "";
    private String subcommandDescription = "";

    private boolean prependPrefix = true;

    public MessageBuilder(){
        this.message = null;
    }
    public MessageBuilder(Message message) {
        this.message = message;
    }

    public MessageBuilder message(Message message) {
        this.message = message;
        return this;
    }

    public MessageBuilder plot(Plot plot) {
        this.plotID = plot.getID();
        this.plotOwnerName = Bukkit.getOfflinePlayer(plot.getOwnerUUID()).getName();
        this.worldName = plot.getWorldName();
        return this;
    }
    public MessageBuilder plotID(String plotID){
        this.plotID = plotID;
        return this;
    }
    public MessageBuilder plotOwnerName(String plotOwnerName) {
        this.plotOwnerName = plotOwnerName;
        return this;
    }
    public MessageBuilder world(String worldName) {
        this.worldName = worldName;
        return this;
    }


    public MessageBuilder user(User user) {
        this.userName = user.getName();
        return this;
    }
    public MessageBuilder username(String username) {
        this.userName = username;
        return this;
    }


    public MessageBuilder group(Group group) {
        this.groupName = group.getName();
        return this;
    }
    public MessageBuilder groupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public MessageBuilder subcommand(Subcommand subcommand) {
        this.subcommandSyntax = subcommand.getSyntax();
        this.subcommandDescription = subcommand.getDescription();
        return this;
    }

    public MessageBuilder noPrependedPrefix() {
        this.prependPrefix = false;
        return this;
    }

    public String build(){
        String message = "";
        if(Config.getInstance().isUsingMessagePrefix() && prependPrefix){
            message += "{prefix}{prefixSpacer}";
        }

        message += Config.getInstance().getMessage(this.message);
        message = message.replace("{prefix}", Config.getInstance().getPrefix());
        message = message.replace("{prefixSpacer}", Config.getInstance().getPrefixSpacer());
        message = message.replace("{plotOwnerName}", plotOwnerName);
        message = message.replace("{plotOwner}", plotOwnerName);
        message = message.replace("{plotID}", plotID);
        message = message.replace("{world}", worldName);
        message = message.replace("{worldName}", worldName);

        message = message.replace("{groupName}", groupName);
        message = message.replace("{group}", groupName);

        message = message.replace("{user}", userName);
        message = message.replace("{userName}", userName);

        message = message.replace("{subcommandSyntax}", subcommandSyntax);
        message = message.replace("{subcommandDescription}", subcommandDescription);

        return TextUtil.fixColors(message);
    }

    public void send(CommandSender receiver) {
        receiver.sendMessage(build());
    }
}