package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands;

import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.config.lang.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

public class PlotHelpCommand implements Subcommand {
	private List<Subcommand> subcommands;

	public PlotHelpCommand() {
		subcommands = null;
	}
	PlotHelpCommand(List<Subcommand> subcommands) {
		this.subcommands = subcommands;
	}

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
		return "/plot help";
	}

	@Override
	public String getPermission() {
		return "chunkyplots.player";
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
			new MessageBuilder(Message.WIDE_HEADER).noPrependedPrefix().send(sender);
			for(Subcommand s: subcommands){
				new MessageBuilder(Message.HELP_COMMAND_ITEM).noPrependedPrefix().subcommand(s).send(sender);
			}
			new MessageBuilder(Message.WIDE_HEADER).noPrependedPrefix().send(sender);
		} else {
			new MessageBuilder(Message.HELP_COMMAND_ERROR).send(sender);
		}
	}

	public void setSubcommands(List<Subcommand> subcommands) {
		this.subcommands = subcommands;
	}
}
