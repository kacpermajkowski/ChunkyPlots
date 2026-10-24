package pl.kacpermajkowski.ChunkyPlots.messages;

import net.kyori.adventure.text.Component;
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
    private String plotMnemonic = "";

    private String userName = "";
    private String groupName = "";

    private String subcommandSyntax = "";
    private String subcommandDescription = "";

    private String plotLimit = "";
    private String currentPlots = "";

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
        if (plot == null) return this;
        this.plotID = plot.getID() != null ? plot.getID() : "";
        this.plotOwnerName = plot.getOwnerName() != null ? plot.getOwnerName() : "";
        this.worldName = plot.getWorldName() != null ? plot.getWorldName() : "";
        this.plotMnemonic = plot.getMnemonicId() != null ? plot.getMnemonicId() : "";
        return this;
    }
    public MessageBuilder plotID(String plotID){
        this.plotID = plotID != null ? plotID : "";
        return this;
    }
    public MessageBuilder plotMnemonic(String plotMnemonic){
        this.plotMnemonic = plotMnemonic != null ? plotMnemonic : "";
        return this;
    }
    public MessageBuilder plotOwnerName(String plotOwnerName) {
        this.plotOwnerName = plotOwnerName != null ? plotOwnerName : "";
        return this;
    }
    public MessageBuilder world(String worldName) {
        this.worldName = worldName != null ? worldName : "";
        return this;
    }

    public MessageBuilder user(Player userPlayer){
        return username(userPlayer != null ? userPlayer.getName() : "");
    }
    public MessageBuilder user(User user) {
        return username(user != null ? user.getName() : "");
    }
    public MessageBuilder username(String username) {
        this.userName = username != null ? username : "";
        return this;
    }

    public MessageBuilder group(Group group) {
        return groupName(group != null ? group.getName() : "");
    }
    public MessageBuilder groupName(String groupName) {
        this.groupName = groupName != null ? groupName : "";
        return this;
    }

    public MessageBuilder subcommand(Subcommand subcommand) {
        if (subcommand != null) {
            this.subcommandSyntax = subcommand.getSyntax() != null ? subcommand.getSyntax() : "";
            this.subcommandDescription = subcommand.getDescription() != null ? subcommand.getDescription() : "";
        }
        return this;
    }

    public MessageBuilder plotLimit(int limit) {
        this.plotLimit = limit == Integer.MAX_VALUE ? "∞" : String.valueOf(limit);
        return this;
    }

    public MessageBuilder currentPlots(int current) {
        this.currentPlots = String.valueOf(current);
        return this;
    }

    public MessageBuilder noPrependedPrefix() {
        this.prependPrefix = false;
        return this;
    }

    private String buildRaw() {
        String message = "";
        if(config != null && config.isUsingMessagePrefix() && prependPrefix){
            message += "{prefix}{prefixSpacer}";
        }

        String msgTemplate = config != null ? config.getMessage(this.message) : (this.message != null ? this.message.name() : "");
        message += msgTemplate != null ? msgTemplate : "";
        if(config != null) {
            message = message.replace("{prefix}", config.getPrefix() != null ? config.getPrefix() : "");
            message = message.replace("{prefixSpacer}", config.getPrefixSpacer() != null ? config.getPrefixSpacer() : "");
        } else {
            message = message.replace("{prefix}", "");
            message = message.replace("{prefixSpacer}", "");
        }
        message = message.replace("{plotOwnerName}", plotOwnerName != null ? plotOwnerName : "");
        message = message.replace("{plotOwner}", plotOwnerName != null ? plotOwnerName : "");
        message = message.replace("{plotID}", plotID != null ? plotID : "");
        message = message.replace("{plotMnemonic}", plotMnemonic != null ? plotMnemonic : "");
        message = message.replace("{mnemonic}", plotMnemonic != null ? plotMnemonic : "");
        message = message.replace("{world}", worldName != null ? worldName : "");
        message = message.replace("{worldName}", worldName != null ? worldName : "");

        message = message.replace("{groupName}", groupName != null ? groupName : "");
        message = message.replace("{group}", groupName != null ? groupName : "");

        message = message.replace("{user}", userName != null ? userName : "");
        message = message.replace("{userName}", userName != null ? userName : "");

        message = message.replace("{subcommandSyntax}", subcommandSyntax != null ? subcommandSyntax : "");
        message = message.replace("{subcommandDescription}", subcommandDescription != null ? subcommandDescription : "");

        message = message.replace("{plotLimit}", plotLimit != null ? plotLimit : "");
        message = message.replace("{currentPlots}", currentPlots != null ? currentPlots : "");

        return message;
    }

    public String build(){
        return TextUtil.fixColors(buildRaw());
    }

    public Component buildComponent() {
        return TextUtil.toComponent(buildRaw());
    }

    public void sendChat(CommandSender receiver) {
        if(receiver == null){
            if (Bukkit.getServer() != null) Bukkit.getLogger().warning("MessageBuilder.sendChat(): receiver is null");
            return;
        }
        receiver.sendMessage(buildComponent());
    }

    public void sendChat(User user){
        if(user == null){
            if (Bukkit.getServer() != null) Bukkit.getLogger().warning("MessageBuilder.sendChat(): user is null");
            return;
        }

        Player receiver = user.getPlayer();
        if(receiver == null){
            if (Bukkit.getServer() != null) Bukkit.getLogger().warning("MessageBuilder.sendChat() called for a user that's not online.");
            return;
        }

        receiver.sendMessage(buildComponent());
    }

    public void sendActionBar(Player player) {
        if (player == null) return;
        TextUtil.sendActionBar(player, buildComponent());
    }

    public void sendBossBar(Player player) {
        if (player == null) return;
        TextUtil.sendBossBarMessage(player, buildComponent());
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
