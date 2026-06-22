package pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands.help;

import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class PlotAdminHelpCommand implements Subcommand {
    ArrayList<Subcommand> subcommands;

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "lista dostępnych komend";
    }

    @Override
    public String getSyntax() {
        return "/plotadmin help";
    }

    @Override
    public String getPermission() {
        return "chunkyplots.plotadmin";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sendHelpMessage(sender);
    }

    @Override
    public List<String> getTabCompletion(CommandSender sender, String[] args) {
        return List.of();
    }

    public void sendHelpMessage(CommandSender sender){
        if(subcommands != null) {
            new MessageBuilder(Message.WIDE_HEADER).noPrependedPrefix().sendChat(sender);
            for(Subcommand s: subcommands){
                new MessageBuilder(Message.HELP_COMMAND_ITEM).noPrependedPrefix().subcommand(s).sendChat(sender);
            }
            new MessageBuilder(Message.WIDE_HEADER).noPrependedPrefix().sendChat(sender);
        } else {
            new MessageBuilder(Message.HELP_COMMAND_ERROR).sendChat(sender);
        }
    }

    public void updateSubcommandList(ArrayList<Subcommand> subcommands) {
        this.subcommands = subcommands;
    }
}
