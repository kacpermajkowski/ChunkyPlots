package pl.kacpermajkowski.ChunkyPlots.commands.plot;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.util.MessageBuilder;

import java.util.List;

public interface PlotSubcommand extends Subcommand {
    @Override
    default void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            execute((Player) sender, args);
        } else {
            new MessageBuilder(Message.SENDER_NOT_PLAYER).send(sender);
        }
    }

    @Override
    default List<String> getTabCompletion(CommandSender sender, String[] args){
        if (sender instanceof Player) {
            return getTabCompletion((Player) sender, args);
        } else {
            return List.of();
        }
    }

    List<String> getTabCompletion(Player player, String[] args);
    void execute(Player player, String[] args);
}
