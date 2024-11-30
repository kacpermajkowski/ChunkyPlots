package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands;

import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.manager.ConfigManager;
import pl.kacpermajkowski.ChunkyPlots.manager.MessageManager;

import java.util.ArrayList;

public class PlotHelpCommand extends Subcommand {
	final ConfigManager configManager = ChunkyPlots.plugin.configManager;
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

	public void sendHelpMessage(CommandSender sender){
		if(subcommands != null) {
			MessageManager.sendNoPrefixMessage(sender, "&9-----------{ " + ChunkyPlots.plugin.configManager.getPluginPrefix() + " &9}-----------");
			for(Subcommand s: subcommands){
				MessageManager.sendNoPrefixMessage(sender, "&a/plot " + s.getName() + " &8- &7" + s.getDescription());
			}
			MessageManager.sendNoPrefixMessage(sender, "&9-----------{ " + ChunkyPlots.plugin.configManager.getPluginPrefix() + " &9}-----------");
		} else {
			MessageManager.sendMessage(sender, "&cSpecified subcommand was not found.");
		}
	}

	public void updateSubcommandList(ArrayList<Subcommand> subcommands) {
		this.subcommands = subcommands;
	}
}
