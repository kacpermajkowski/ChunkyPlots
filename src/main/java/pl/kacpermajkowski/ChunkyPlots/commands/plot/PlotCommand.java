package pl.kacpermajkowski.ChunkyPlots.commands.plot;

import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.commands.Command;

public class PlotCommand extends Command {
	@Override
	public void execute(CommandSender sender) {
		PlotCommandManager.sendHelpMessage(sender);
	}
}
