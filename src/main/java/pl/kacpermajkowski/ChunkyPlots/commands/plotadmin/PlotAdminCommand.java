package pl.kacpermajkowski.ChunkyPlots.commands.plotadmin;

import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.commands.Command;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotCommandManager;

public class PlotAdminCommand extends Command {
	@Override
	public void execute(CommandSender sender) {
		PlotAdminCommandManager.sendHelpMessage(sender);
	}
}
