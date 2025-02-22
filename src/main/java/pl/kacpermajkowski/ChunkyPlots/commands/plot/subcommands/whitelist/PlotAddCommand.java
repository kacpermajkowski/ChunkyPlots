package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.whitelist;

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
import java.util.UUID;

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

        String nameToWhitelist = args[0];
        messageBuilder = messageBuilder.username(nameToWhitelist);
        OfflinePlayer playerToWhitelist;
        try {
            playerToWhitelist = OfflinePlayerUtil.getOfflinePlayer(nameToWhitelist);
        } catch (IllegalArgumentException e) {
            messageBuilder.message(Message.ERROR_UNSPECIFIED).send(playerOwner);
            return;
        }
        if(playerToWhitelist == null) {
            messageBuilder.message(Message.NULL_USER).send(playerOwner);
            return;
        }

        if(plot.isPlayerWhitelisted(playerToWhitelist)) {
            messageBuilder.message(Message.PLAYER_IS_ALREADY_A_MEMBER).send(playerOwner);
            return;
        }

        if(plot.isPlayerOwner(playerToWhitelist)) {
            messageBuilder.message(Message.CANNOT_ADD_OWNER_AS_MEMBER).send(playerOwner);
            return;
        }

        if(plot.isPlayerBlacklisted(playerToWhitelist)) {
            messageBuilder.message(Message.CANNOT_ADD_BLACKLISTED_PLAYER_AS_MEMBER).send(playerOwner);
            return;
        }

        plot.whitelistPlayer(playerToWhitelist.getUniqueId());
        messageBuilder.message(Message.ADDED_MEMBER_TO_PLOT).send(playerOwner);
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
