package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.blacklist;

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

public class PlotDenyCommand implements PlotSubcommand {
    @Override
    public String getName() {
        return "deny";
    }

    @Override
    public String getDescription() {
        return "pozwala zablokować graczowi wstep na działkę";
    }

    @Override
    public String getSyntax() {
        return "/plot deny <player>";
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

        String nameToBlacklist = args[0];
        messageBuilder.username(nameToBlacklist);
        OfflinePlayer playerToBlacklist;
        try {
            playerToBlacklist = PlayerUtil.getOfflinePlayer(nameToBlacklist);
        } catch (IllegalArgumentException e) {
            messageBuilder.message(Message.ERROR_UNSPECIFIED).sendChat(playerOwner);
            return;
        }
        if(playerToBlacklist == null) {
            messageBuilder.message(Message.NULL_USER).sendChat(playerOwner);
            return;
        }

        if(plot.isPlayerOwner(playerToBlacklist)) {
            messageBuilder.message(Message.CANNOT_ADD_OWNER_TO_BLACKLIST).sendChat(playerOwner);
            return;
        }

        if(plot.isPlayerBlacklisted(playerToBlacklist)) {
            messageBuilder.message(Message.PLAYER_IS_ALREADY_BLACKLISTED).username(nameToBlacklist).sendChat(playerOwner);
            return;
        }

        if(plot.isPlayerWhitelisted(playerToBlacklist)) {
            plot.unwhitelistPlayer(playerToBlacklist);
            messageBuilder.message(Message.PLAYER_WAS_WHITELISTED).username(nameToBlacklist).sendChat(playerOwner);
        }

        plot.blacklistPlayer(playerToBlacklist);
        messageBuilder.message(Message.BLACKLIST_ADDED_TO_PLOT).sendChat(playerOwner);
    }


    @Override
    public List<String> getTabCompletion(Player player, String[] args) {
        Plot plot = PlotManager.getInstance().getPlot(player.getLocation());
        if(args.length == 1) {
            if(plot != null) {
                if(plot.isPlayerOwner(player)){
                    List<String> completions = new ArrayList<>();
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(!p.getName().equals(player.getName()) && !plot.isPlayerBlacklisted(p)) {
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
