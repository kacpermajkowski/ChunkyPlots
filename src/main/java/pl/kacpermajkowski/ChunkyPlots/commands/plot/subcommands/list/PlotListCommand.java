package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.list;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.shared.PlotSharedCommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

import java.util.Arrays;
import java.util.List;

public class PlotListCommand implements PlotSubcommand {
	private final PlotManager plotManager;
	private final PlotSharedCommand sharedCommand;

	public PlotListCommand(PlotManager plotManager) {
		this(plotManager, new PlotSharedCommand(plotManager));
	}

	public PlotListCommand(PlotManager plotManager, PlotSharedCommand sharedCommand) {
		this.plotManager = plotManager;
		this.sharedCommand = sharedCommand;
	}

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public String getDescription() {
		return "lista działek gracza";
	}

	@Override
	public String getSyntax() {
		return "/plot list [member]";
	}

	@Override
	public String getPermission() {
		return "chunkyplots.player";
	}

	@Override
	public void execute(Player sender, String[] args) {
		if (args.length >= 1 && (args[0].equalsIgnoreCase("member") || args[0].equalsIgnoreCase("shared") || args[0].equalsIgnoreCase("memberplots"))) {
			String[] remainingArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
			if (sharedCommand != null) {
				sharedCommand.execute(sender, remainingArgs);
			} else {
				new PlotSharedCommand(plotManager).execute(sender, remainingArgs);
			}
			return;
		}

		if (sender instanceof Player player) {
			new MessageBuilder(Message.WIDE_HEADER).sendChat(player);
			player.sendMessage(TextUtil.toComponent("&eID Działki &8(&eKod&8) &9» &eKoordynaty &8(kliknij, aby zarządzać)"));
			List<Plot> ownedPlots = plotManager != null ? plotManager.getPlotsByOwner(player.getUniqueId()) : List.of();
			boolean hasPlots = !ownedPlots.isEmpty();
			for (Plot plot : ownedPlots) {
				int blockX = (plot.getChunkX() << 4) + 8;
				int blockZ = (plot.getChunkZ() << 4) + 8;

				Component line = Component.text()
						.append(Component.text("(", NamedTextColor.DARK_GRAY))
						.append(Component.text(plot.getID(), NamedTextColor.GOLD))
						.append(Component.text(") [", NamedTextColor.DARK_GRAY))
						.append(Component.text(plot.getMnemonicId(), NamedTextColor.YELLOW))
						.append(Component.text("] ", NamedTextColor.DARK_GRAY))
						.append(Component.text("» ", NamedTextColor.GREEN))
						.append(Component.text("X:", NamedTextColor.GRAY))
						.append(Component.text(blockX + " ", NamedTextColor.WHITE))
						.append(Component.text("Z:", NamedTextColor.GRAY))
						.append(Component.text(blockZ + " ", NamedTextColor.WHITE))
						.append(Component.text("[" + plot.getWorldName() + "]", NamedTextColor.DARK_AQUA))
						.hoverEvent(HoverEvent.showText(Component.text("Kliknij, aby zarządzać działką /plot info " + plot.getMnemonicId(), NamedTextColor.YELLOW)))
						.clickEvent(ClickEvent.runCommand("/plot info " + plot.getMnemonicId()))
						.build();

				player.sendMessage(line);
			}
			if (!hasPlots) {
				player.sendMessage(TextUtil.toComponent("&7Nie posiadasz żadnych zarejestrowanych działek."));
			}
			new MessageBuilder(Message.WIDE_HEADER).sendChat(player);
		}
	}

	@Override
	public List<String> getTabCompletion(Player player, String[] args) {
		if (args.length == 1) {
			List<String> options = List.of("member", "shared");
			return options.stream()
					.filter(s -> s.startsWith(args[0].toLowerCase()))
					.toList();
		}
		return List.of();
	}
}
