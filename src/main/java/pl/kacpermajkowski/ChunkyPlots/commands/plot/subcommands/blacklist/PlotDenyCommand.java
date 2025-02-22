package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.blacklist;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.config.lang.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.OfflinePlayerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            messageBuilder.message(Message.COMMAND_USAGE).send(playerOwner);
            return;
        }

        Plot plot = PlotManager.getInstance().getPlot(playerOwner.getLocation());
        if(plot == null) {
            messageBuilder.message(Message.NULL_PLOT).send(playerOwner);
            return;
        }
        if(!plot.isPlayerOwner(playerOwner)){
            messageBuilder.message(Message.NOT_OWNER).send(playerOwner);
            return;
        }

        String nameToBlacklist = args[0];
        messageBuilder.username(nameToBlacklist);
        OfflinePlayer playerToBlacklist;
        try {
            playerToBlacklist = OfflinePlayerUtil.getOfflinePlayer(nameToBlacklist);
        } catch (IllegalArgumentException e) {
            messageBuilder.message(Message.ERROR_UNSPECIFIED).send(playerOwner);
            return;
        }
        if(playerToBlacklist == null) {
            messageBuilder.message(Message.NULL_USER).send(playerOwner);
            return;
        }

        if(plot.isPlayerOwner(playerToBlacklist)) {
            messageBuilder.message(Message.CANNOT_ADD_OWNER_TO_BLACKLIST).send(playerOwner);
            return;
        }

        if(plot.isPlayerBlacklisted(playerToBlacklist)) {
            messageBuilder.message(Message.PLAYER_IS_ALREADY_BLACKLISTED).username(nameToBlacklist).send(playerOwner);
            return;
        }

        if(plot.isPlayerWhitelisted(playerToBlacklist)) {
            plot.unwhitelistPlayer(playerToBlacklist);
            messageBuilder.message(Message.PLAYER_WAS_WHITELISTED).username(nameToBlacklist).send(playerOwner);
        }

        plot.blacklistPlayer(playerToBlacklist);
        messageBuilder.message(Message.BLACKLIST_ADDED_TO_PLOT).send(playerOwner);
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
