package pl.kacpermajkowski.ChunkyPlots.messages;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.MessageDisplayType;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.group.Group;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.user.User;

import java.util.Set;

public class MessageBuilder {
    private static Config config;

    public static void init(Config cfg) {
        config = cfg;
    }

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


    public MessageBuilder user(Player userPlayer){
        return username(userPlayer.getName());
    }
    public MessageBuilder user(User user) {
        return username(user.getName());
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
        if(config != null && config.isUsingMessagePrefix() && prependPrefix){
            message += "{prefix}{prefixSpacer}";
        }

        String msgTemplate = config != null ? config.getMessage(this.message) : (this.message != null ? this.message.name() : "");
        message += msgTemplate != null ? msgTemplate : "";
        if(config != null) {
            message = message.replace("{prefix}", config.getPrefix());
            message = message.replace("{prefixSpacer}", config.getPrefixSpacer());
        } else {
            message = message.replace("{prefix}", "");
            message = message.replace("{prefixSpacer}", "");
        }
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

    public void sendChat(CommandSender receiver) {
        if(receiver == null){
            Bukkit.getLogger().warning("MessageBuilder.sendChat(): receiver is null");
            return;
        }
        receiver.sendMessage(build());
    }

    public void sendChat(User user){
        if(user == null){
            Bukkit.getLogger().warning("MessageBuilder.sendChat(): user is null");
            return;
        }

        Player receiver = user.getPlayer();
        if(receiver == null){
            Bukkit.getLogger().warning("MessageBuilder.sendChat() called for a user that's not online.");
            return;
        }

        receiver.sendMessage(build());
    }

    public void sendActionBar(Player player) {
        TextUtil.sendActionBarMessage(player, build());
    }

    public void sendBossBar(Player player) {
        TextUtil.sendBossBarMessage(player, build());
    }

    public void sendAll(CommandSender receiver) {
        if(!(receiver instanceof Player player)){
            sendChat(receiver);
            return;
        }

        Set<MessageDisplayType> types = config != null ? config.getMessageDisplayTypes() : Set.of(MessageDisplayType.CHAT);

        if(types.contains(MessageDisplayType.CHAT)){
            sendChat(receiver);
        }
        if(types.contains(MessageDisplayType.ACTIONBAR)){
            sendActionBar(player);
        }
        if(types.contains(MessageDisplayType.BOSSBAR)){
            sendBossBar(player);
        }
    }
}
