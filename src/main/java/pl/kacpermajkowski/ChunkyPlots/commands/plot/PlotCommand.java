package pl.kacpermajkowski.ChunkyPlots.commands.plot;

import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.commands.Command;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlotCommand extends Command<PlotCommand> {
	private static PlotCommand instance;

	private final ArrayList<Subcommand> subcommands;
	private PlotCommand() {
		instance = this;

		this.subcommands = new ArrayList<>();
		PlotHelpCommand help = new PlotHelpCommand();
		subcommands.add(help);
		subcommands.add(new PlotDisposeCommand());
		subcommands.add(new PlotBlacklistCommand());
		subcommands.add(new PlotFlagCommand());
		subcommands.add(new PlotGroupCommand());
		subcommands.add(new PlotInfoCommand());
		subcommands.add(new PlotMembersCommand());
		subcommands.add(new PlotVisitCommand());
		subcommands.add(new PlotListCommand());
		help.setSubcommands(subcommands);
	}

	@Override
	protected void execute(CommandSender sender, String[] args) {
		getSubcommand("help").execute(sender, args);
	}

	protected List<Subcommand> getSubcommands() {
		return Collections.unmodifiableList(subcommands);
	}

	public static Subcommand getHelpSubcommand() {
		return instance.getSubcommand("help");
	}

	public static PlotCommand getInstance() {
		if(instance == null) {
			instance = new PlotCommand();
		}
		return instance;
	}
}
