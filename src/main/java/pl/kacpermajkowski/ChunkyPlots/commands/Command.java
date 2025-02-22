package pl.kacpermajkowski.ChunkyPlots.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.util.MessageBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Command<T extends Command<T>> implements TabExecutor {
	protected abstract void execute(CommandSender sender, String[] args);
	protected abstract List<Subcommand> getSubcommands();

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String s, String[] args) {
		if(!(sender instanceof Player)){
			new MessageBuilder(Message.SENDER_NOT_PLAYER).send(sender);
			return true;
		}

		if(args.length == 0) {
			this.execute(sender, args);
			return true;
		}

		Subcommand subcommand = getSubcommand(args[0]);
		if(subcommand == null) {
			new MessageBuilder(Message.SUBCOMMAND_NOT_FOUND).send(sender);
			return true;
		}

		if (!sender.hasPermission(subcommand.getPermission())) {
			new MessageBuilder(Message.INSUFFICIENT_PERMISSIONS).send(sender);
			return true;
		}

		subcommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
		return true;
	}

	protected Subcommand getSubcommand(String providedSubcommandName) {
		List<Subcommand> matchingSubcommands = getNameMatchingSubcommands(providedSubcommandName);
		if(matchingSubcommands.size() == 1) {
			return matchingSubcommands.getFirst();
		} else if(matchingSubcommands.size() > 1) {
			throw new IllegalStateException("There's more than one subcommand registered under the name " + providedSubcommandName);
		} else {
			return null;
		}
	}

	private List<Subcommand> getNameMatchingSubcommands(String providedSubcommandName) {
		List<Subcommand> matchingSubcommands = new ArrayList<>();
		for(Subcommand subcommand : getSubcommands()) {
			if(subcommand.getName().equalsIgnoreCase(providedSubcommandName)) {
				matchingSubcommands.add(subcommand);
			}
		}
		return matchingSubcommands;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
		List<String> autocomParams = new ArrayList<>();
		if (args.length == 1) {
			for (Subcommand subcommand : getSubcommands()) {
				if(sender.hasPermission(subcommand.getPermission())) {
					autocomParams.add(subcommand.getName());
				}
			}

		} else if (args.length >= 2) {
			Subcommand subcommand = getSubcommand(args[0]);
			if (subcommand != null) {
				autocomParams = subcommand.getTabCompletion(sender, Arrays.copyOfRange(args, 1, args.length));
			}
		}
		return autocomParams;
	}
}
