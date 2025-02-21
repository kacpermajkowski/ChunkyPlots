package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.deny;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.config.lang.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.OfflinePlayerUtil;

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
    public void execute(Player player, String[] args) {
        MessageBuilder messageBuilder = new MessageBuilder().subcommand(this);

        if(args.length != 1) {
            messageBuilder.message(Message.COMMAND_USAGE).send(player);
            return;
        }

        Plot plot = PlotManager.getInstance().getPlot(player.getLocation());
        if(plot == null) {
            messageBuilder.message(Message.NULL_PLOT).send(player);
            return;
        }
        if(!plot.isPlayerOwner(player)){
            messageBuilder.message(Message.NOT_OWNER).send(player);
            return;
        }

        String nameToBlacklist = args[0];
        OfflinePlayer playerToBlacklist;
        try {
            playerToBlacklist = OfflinePlayerUtil.getOfflinePlayer(nameToBlacklist);
        } catch (IllegalArgumentException e) {
            messageBuilder.message(Message.ERROR_UNSPECIFIED).send(player);
            return;
        }
        if(playerToBlacklist == null) {
            messageBuilder.message(Message.NULL_USER).send(player);
            return;
        }

        if(plot.isPlayerOwner(playerToBlacklist)) {
            messageBuilder.message(Message.CANNOT_ADD_OWNER_TO_BLACKLIST).send(player);
        }

        if(plot.getBlacklist().contains(player.getUniqueId())) {
            messageBuilder.message(Message.PLAYER_IS_ALREADY_BLACKLISTED).username(nameToBlacklist).send(player);
            return;
        }

        plot.getBlacklist().add(playerToBlacklist.getUniqueId());
        messageBuilder.message(Message.BLACKLIST_ADDED_TO_PLOT).send(player);
    }


    @Override
    public List<String> getTabCompletion(CommandSender sender, String[] args) {
        // Returning null displays completion of all online players
        if(args.length == 0) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        return List.of();
    }
}
