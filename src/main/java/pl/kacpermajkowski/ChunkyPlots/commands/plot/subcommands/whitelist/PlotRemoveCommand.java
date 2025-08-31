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
import java.util.UUID;

public class PlotRemoveCommand implements PlotSubcommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "pozwala na usunięcie gracza z białej listy";
    }

    @Override
    public String getSyntax() {
        return "/plot remove <player>";
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

        String nameToUnwhitelist = args[0];
        messageBuilder = messageBuilder.username(nameToUnwhitelist);
        OfflinePlayer playerToUnwhitelist;
        try {
            playerToUnwhitelist = PlayerUtil.getOfflinePlayer(nameToUnwhitelist);
        } catch (IllegalArgumentException e) {
            messageBuilder.message(Message.ERROR_UNSPECIFIED).sendChat(playerOwner);
            return;
        }
        if(playerToUnwhitelist == null) {
            messageBuilder.message(Message.NULL_USER).sendChat(playerOwner);
            return;
        }

        if(!plot.isPlayerWhitelisted(playerToUnwhitelist)) {
            messageBuilder.message(Message.PLAYER_IS_NOT_WHITELISTED).username(nameToUnwhitelist).sendChat(playerOwner);
            return;
        }

        plot.unwhitelistPlayer(playerToUnwhitelist.getUniqueId());
        messageBuilder.message(Message.REMOVED_MEMBER_FROM_PLOT).sendChat(playerOwner);
    }



    @Override
    public List<String> getTabCompletion(Player player, String[] args) {
        Plot plot = PlotManager.getInstance().getPlot(player.getLocation());
        if(args.length == 1) {
            if(plot != null) {
                if(plot.isPlayerOwner(player)){
                    List<String> completions = new ArrayList<>();
                    for(UUID playerUUID : plot.getWhitelist()){
                        completions.add(Bukkit.getOfflinePlayer(playerUUID).getName());
                    }
                    return completions;
                }
            }
        }
        return List.of();
    }
}
