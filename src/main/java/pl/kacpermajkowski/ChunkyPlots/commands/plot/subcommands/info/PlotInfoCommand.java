package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.info;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlotInfoCommand implements PlotSubcommand {
	private final PlotManager plotManager;

	public PlotInfoCommand(PlotManager plotManager) {
		this.plotManager = plotManager;
	}

	@Override
	public String getName() {
		return "info";
	}

	@Override
	public String getDescription() {
		return "wyświetla informacje o działce";
	}

	@Override
	public String getSyntax() {
		return "/plot info [id]";
	}

	@Override
	public String getPermission() {
		return "chunkyplots.player";
	}

	@Override
	public void execute(Player sender, String[] args) {
		if(sender instanceof Player player) {
			Plot plot;
			if (args.length > 0) {
				plot = plotManager.getPlot(args[0]);
			} else {
				plot = plotManager.getPlot(player.getLocation().getChunk());
			}

			if (plot != null) {
				new MessageBuilder(Message.WIDE_HEADER).noPrependedPrefix().sendChat(player);
				player.sendMessage(TextUtil.toComponent("&aWłaściciel: &f" + Bukkit.getOfflinePlayer(plot.getOwnerUUID()).getName()));
				player.sendMessage(TextUtil.toComponent("&aID Działki: &6" + plot.getID()));
				player.sendMessage(TextUtil.toComponent("&aIdentyfikator: &e" + plot.getMnemonicId()));
				player.sendMessage(TextUtil.toComponent("&aChunk: &7X:&f" + plot.getChunkX() + "  &7Z:&f" + plot.getChunkZ()));
				player.sendMessage(TextUtil.toComponent("&aŚwiat: &f" + plot.getWorldName()));
				player.sendMessage(TextUtil.toComponent("&aCzłonkowie: &f" + plot.getWhitelist().stream().map((uuid) ->
					Bukkit.getOfflinePlayer(uuid).getName()
				).toList()));
				player.sendMessage(TextUtil.toComponent("&aZablokowani: &f" + plot.getBlacklist().stream().map((uuid) ->
						Bukkit.getOfflinePlayer(uuid).getName()
				).toList()));
				Map<PlotFlag, Boolean> customFlags = plot.getCustomFlags();
				if (!customFlags.isEmpty()) {
					String flagsStr = customFlags.entrySet().stream()
							.map(e -> e.getKey().getName() + ": " + (e.getValue() ? "&aON&f" : "&cOFF&f"))
							.collect(Collectors.joining(", "));
					player.sendMessage(TextUtil.toComponent("&aZmienione flagi: &f" + flagsStr));
				}
				int blockX = (plot.getChunkX() << 4) + 8;
				int blockZ = (plot.getChunkZ() << 4) + 8;
				player.sendMessage(TextUtil.toComponent("&aŚrodek: &7X:&f" + blockX + "  &7Z:&f" + blockZ));
				new MessageBuilder(Message.WIDE_HEADER).noPrependedPrefix().sendChat(player);
			} else {
				new MessageBuilder(Message.NULL_PLOT).sendChat(sender);
			}
		}
	}

	@Override
	public List<String> getTabCompletion(Player player, String[] args) {
		if (args.length == 1) {
			List<String> completions = new ArrayList<>();
			for (Plot plot : plotManager.getPlots()) {
				if (plot.getID() != null) completions.add(plot.getID());
				if (plot.getMnemonicId() != null) completions.add(plot.getMnemonicId());
			}
			return completions.stream()
					.filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
					.toList();
		}
		return List.of();
	}
}
