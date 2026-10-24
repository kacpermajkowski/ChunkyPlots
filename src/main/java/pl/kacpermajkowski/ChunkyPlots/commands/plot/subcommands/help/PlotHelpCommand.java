package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.help;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;

import java.util.List;

public class PlotHelpCommand implements PlotSubcommand {
	private List<PlotSubcommand> subcommands;

	public PlotHelpCommand() {
		subcommands = null;
	}
	PlotHelpCommand(List<PlotSubcommand> subcommands) {
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
	public void execute(Player sender, String[] args) {
		sendHelpMessage(sender);
	}

	@Override
	public List<String> getTabCompletion(Player player, String[] args) {
		return List.of();
	}

	public void sendHelpMessage(CommandSender sender){
		if(subcommands != null) {
			new MessageBuilder(Message.WIDE_HEADER).noPrependedPrefix().sendChat(sender);
			for(Subcommand s: subcommands){
				Component item = Component.text()
						.append(Component.text(s.getSyntax(), NamedTextColor.GREEN))
						.append(Component.text(" - ", NamedTextColor.DARK_GRAY))
						.append(Component.text(s.getDescription(), NamedTextColor.GRAY))
						.hoverEvent(HoverEvent.showText(Component.text("Kliknij, aby wstawić komendę do czatu", NamedTextColor.YELLOW)))
						.clickEvent(ClickEvent.suggestCommand(s.getSyntax()))
						.build();
				sender.sendMessage(item);
			}
			new MessageBuilder(Message.WIDE_HEADER).noPrependedPrefix().sendChat(sender);
		} else {
			new MessageBuilder(Message.HELP_COMMAND_ERROR).sendChat(sender);
		}
	}

	public void setSubcommands(List<PlotSubcommand> subcommands) {
		this.subcommands = subcommands;
	}
}
