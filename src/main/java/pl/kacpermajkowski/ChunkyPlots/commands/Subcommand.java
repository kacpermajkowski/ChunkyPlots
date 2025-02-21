package pl.kacpermajkowski.ChunkyPlots.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface Subcommand{

	String getName();

	String getDescription();

	String getSyntax();

	String getPermission();

	void execute(CommandSender sender, String[] args);

	List<String> getTabCompletion(CommandSender sender, String[] args);
}
