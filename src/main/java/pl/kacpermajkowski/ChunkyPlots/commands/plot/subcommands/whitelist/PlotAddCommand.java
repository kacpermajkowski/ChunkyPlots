package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.whitelist;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.utils.PlayerUtil;

import java.util.ArrayList;
import java.util.List;

public class PlotAddCommand implements PlotSubcommand {
    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "pozwala na dodanie gracza do białej listy";
    }

    @Override
    public String getSyntax() {
        return "/plot add <player>";
    }

    @Override
    public String getPermission() {
        return "chunkyplots.player";
    }

    @Override
    public void execute(Player playerOwner, String[] args) {
        MessageBuilder messageBuilder = new MessageBuilder().subcommand(this);

        if(args.length != 1) {
            messageBuilder.message(Message.COMMAND_USAGE).sendChat(playerOwner);
            return;
        }

        Plot plot = PlotManager.getInstance().getPlot(playerOwner.getLocation());
        if(plot == null) {
            messageBuilder.message(Message.NULL_PLOT).sendChat(playerOwner);
            return;
        }
        if(!plot.isPlayerOwner(playerOwner)){
            messageBuilder.message(Message.NOT_OWNER).sendChat(playerOwner);
            return;
        }

        String nameToWhitelist = args[0];
        messageBuilder = messageBuilder.username(nameToWhitelist);
        OfflinePlayer playerToWhitelist;
        try {
            playerToWhitelist = PlayerUtil.getOfflinePlayer(nameToWhitelist);
        } catch (IllegalArgumentException e) {
            messageBuilder.message(Message.ERROR_UNSPECIFIED).sendChat(playerOwner);
            return;
        }
        if(playerToWhitelist == null) {
            messageBuilder.message(Message.NULL_USER).sendChat(playerOwner);
            return;
        }

        if(plot.isPlayerWhitelisted(playerToWhitelist)) {
            messageBuilder.message(Message.PLAYER_IS_ALREADY_A_MEMBER).sendChat(playerOwner);
            return;
        }

        if(plot.isPlayerOwner(playerToWhitelist)) {
            messageBuilder.message(Message.CANNOT_ADD_OWNER_AS_MEMBER).sendChat(playerOwner);
            return;
        }

        if(plot.isPlayerBlacklisted(playerToWhitelist)) {
            messageBuilder.message(Message.CANNOT_ADD_BLACKLISTED_PLAYER_AS_MEMBER).sendChat(playerOwner);
            return;
        }

        plot.whitelistPlayer(playerToWhitelist.getUniqueId());
        messageBuilder.message(Message.ADDED_MEMBER_TO_PLOT).sendChat(playerOwner);
    }



    @Override
    public List<String> getTabCompletion(Player player, String[] args) {
        Plot plot = PlotManager.getInstance().getPlot(player.getLocation());
        if(args.length == 1) {
            if(plot != null) {
                if(plot.isPlayerOwner(player)){
                    List<String> completions = new ArrayList<>();
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!p.getName().equals(player.getName()) && !plot.isPlayerWhitelisted(p) && !plot.isPlayerBlacklisted(p)) {
                            completions.add(p.getName());
                        }
                    }
                    return completions;
                }
            }
        }
        return List.of();
    }
}
