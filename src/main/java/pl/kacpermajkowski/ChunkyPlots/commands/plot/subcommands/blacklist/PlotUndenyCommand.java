package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.blacklist;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.config.lang.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.PlayerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotUndenyCommand implements PlotSubcommand {
    @Override
    public String getName() {
        return "undeny";
    }

    @Override
    public String getDescription() {
        return "pozwala przywrócić zablokowanemu graczowi wstep na działkę";
    }

    @Override
    public String getSyntax() {
        return "/plot undeny <player>";
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

        String nameToUnblacklist = args[0];
        messageBuilder = messageBuilder.username(nameToUnblacklist);
        OfflinePlayer playerToUnblacklist;
        try {
            playerToUnblacklist = PlayerUtil.getOfflinePlayer(nameToUnblacklist);
        } catch (IllegalArgumentException e) {
            messageBuilder.message(Message.ERROR_UNSPECIFIED).send(playerOwner);
            return;
        }

        if(playerToUnblacklist == null) {
            messageBuilder.message(Message.NULL_USER).send(playerOwner);
            return;
        }

        if(!plot.isPlayerBlacklisted(playerToUnblacklist)) {
            messageBuilder.message(Message.PLAYER_IS_NOT_BLACKLISTED).username(nameToUnblacklist).send(playerOwner);
            return;
        }

        plot.unblacklistPlayer(playerToUnblacklist.getUniqueId());
        messageBuilder.message(Message.BLACKLIST_REMOVED_FROM_PLOT).send(playerOwner);
    }


    @Override
    public List<String> getTabCompletion(Player player, String[] args) {
        Plot plot = PlotManager.getInstance().getPlot(player.getLocation());
        if(args.length == 1) {
            if(plot != null) {
                if(plot.isPlayerOwner(player)){
                    List<String> completions = new ArrayList<>();
                    for(UUID playerUUID : plot.getBlacklist()){
                        completions.add(Bukkit.getOfflinePlayer(playerUUID).getName());
                    }
                    return completions;
                }
            }
        }
        return List.of();
    }
}
