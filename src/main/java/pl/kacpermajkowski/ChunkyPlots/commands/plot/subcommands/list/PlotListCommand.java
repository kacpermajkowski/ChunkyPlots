package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.list;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

import java.util.List;

public class PlotListCommand implements PlotSubcommand {
	private final PlotManager plotManager;

	public PlotListCommand(PlotManager plotManager) {
		this.plotManager = plotManager;
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
		return "/plot list";
	}

	@Override
	public String getPermission() {
		return "chunkyplots.player";
	}

	@Override
	public void execute(Player sender, String[] args) {
		if(sender instanceof Player player){
			new MessageBuilder(Message.WIDE_HEADER).sendChat(player);
			player.sendMessage(TextUtil.toComponent("&eID Działki &9» &eKoordynaty &8(kliknij, aby zarządzać)"));
			List<Plot> ownedPlots = plotManager.getPlotsByOwner(player.getUniqueId());
			boolean hasPlots = !ownedPlots.isEmpty();
			for(Plot plot : ownedPlots){
				int blockX = (plot.getChunkX() << 4) + 8;
				int blockZ = (plot.getChunkZ() << 4) + 8;

				Component line = Component.text()
						.append(Component.text("(", NamedTextColor.DARK_GRAY))
						.append(Component.text(plot.getID(), NamedTextColor.GOLD))
						.append(Component.text(") ", NamedTextColor.DARK_GRAY))
						.append(Component.text("» ", NamedTextColor.GREEN))
						.append(Component.text("X:", NamedTextColor.GRAY))
						.append(Component.text(blockX + " ", NamedTextColor.WHITE))
						.append(Component.text("Z:", NamedTextColor.GRAY))
						.append(Component.text(blockZ + " ", NamedTextColor.WHITE))
						.append(Component.text("[" + plot.getWorldName() + "]", NamedTextColor.DARK_AQUA))
						.hoverEvent(HoverEvent.showText(Component.text("Kliknij, aby zarządzać działką /plot info " + plot.getID(), NamedTextColor.YELLOW)))
						.clickEvent(ClickEvent.runCommand("/plot info " + plot.getID()))
						.build();

				player.sendMessage(line);
			}
			if(!hasPlots) {
				player.sendMessage(TextUtil.toComponent("&7Nie posiadasz żadnych zarejestrowanych działek."));
			}
			new MessageBuilder(Message.WIDE_HEADER).sendChat(player);
		}
	}

	@Override
	public List<String> getTabCompletion(Player player, String[] args) {
		return List.of();
	}
}
