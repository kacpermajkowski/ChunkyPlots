package pl.kacpermajkowski.ChunkyPlots.commands.plotadmin;

import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.commands.Command;
import pl.kacpermajkowski.ChunkyPlots.commands.CommandManager;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.*;
import pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands.PlotAdminBlockCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands.PlotAdminHelpCommand;

import java.util.ArrayList;

public class PlotAdminCommandManager extends CommandManager {
    public static PlotAdminHelpCommand plotAdminHelpCommand = new PlotAdminHelpCommand();

    protected ArrayList<Subcommand> getSubcommands() {
        ArrayList<Subcommand> subcommands = new ArrayList<>();
        subcommands.add(plotAdminHelpCommand);
        subcommands.add(new PlotAdminBlockCommand());
        plotAdminHelpCommand.updateSubcommandList(subcommands);
        return subcommands;
    }

    @Override
    protected Command getCommand() {
        return new PlotCommand();
    }

    public static void sendHelpMessage(CommandSender sender){
        plotAdminHelpCommand.sendHelpMessage(sender);
    }
}
