package pl.kacpermajkowski.ChunkyPlots.commands.plot;

import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.commands.Command;
import pl.kacpermajkowski.ChunkyPlots.commands.CommandManager;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.*;

import java.util.ArrayList;

public class PlotCommandManager extends CommandManager {
	public static PlotHelpCommand plotHelpCommand = new PlotHelpCommand();

	protected ArrayList<Subcommand> getSubcommands() {
		ArrayList<Subcommand> subcommands = new ArrayList<>();
		subcommands.add(new PlotDisposeCommand());
		subcommands.add(new PlotBlacklistCommand());
		subcommands.add(new PlotFlagCommand());
		subcommands.add(new PlotGroupCommand());
		subcommands.add(new PlotInfoCommand());
		subcommands.add(new PlotMembersCommand());
		subcommands.add(new PlotVisitCommand());
		subcommands.add(new PlotListCommand());
		subcommands.add(plotHelpCommand);
		plotHelpCommand.updateSubcommandList(subcommands);
		return subcommands;
	}

	@Override
	protected Command getCommand() {
		return new PlotCommand();
	}

	public static void sendHelpMessage(CommandSender sender){
		plotHelpCommand.sendHelpMessage(sender);
	}

}
