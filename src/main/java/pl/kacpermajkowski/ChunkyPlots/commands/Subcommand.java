package pl.kacpermajkowski.ChunkyPlots.commands;

import org.bukkit.command.CommandSender;

public abstract class Subcommand{

	public abstract String getName();

	public abstract String getDescription();

	public abstract String getSyntax();

	public abstract String getPermission();

	public abstract void execute(CommandSender sender, String[] args);
}
