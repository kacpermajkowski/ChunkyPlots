package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.info;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.util.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.TextUtil;

import java.util.List;

public class PlotInfoCommand implements PlotSubcommand {
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
		return "/plot info";
	}

	@Override
	public String getPermission() {
		return "chunkyplots.player";
	}

	@Override
	public void execute(Player sender, String[] args) {
		if(sender instanceof Player player) {
 			Plot plot = PlotManager.getInstance().getPlot(player.getLocation().getChunk());
			if (plot != null) {
				new MessageBuilder(Message.WIDE_HEADER).noPrependedPrefix().send(player);
				player.sendMessage(TextUtil.fixColors( "&aWłaściciel: &f" + Bukkit.getOfflinePlayer(plot.getOwnerUUID()).getName()));
				player.sendMessage(TextUtil.fixColors( "&aChunk X: &f" + plot.getChunkX()));
				player.sendMessage(TextUtil.fixColors( "&aChunk Z: &f" + plot.getChunkZ()));
				player.sendMessage(TextUtil.fixColors( "&aŚwiat: &f" + plot.getWorldName()));
				player.sendMessage(TextUtil.fixColors( "&aCzłonkowie: &f" + plot.getWhitelist().stream().map((uuid) ->
					Bukkit.getOfflinePlayer(uuid).getName()
				).toList()));
				player.sendMessage(TextUtil.fixColors( "&aZablokowani: &f" + plot.getBlacklist().stream().map((uuid) ->
						Bukkit.getOfflinePlayer(uuid).getName()
				).toList()));
				Location location = player.getWorld().getChunkAt(plot.getChunkX(), plot.getChunkZ()).getBlock(8, 64, 8).getLocation();
				player.sendMessage(TextUtil.fixColors( "&aLokalizacja: &7X:&f" + location.getBlockX() + "  &7Y:&f" + location.getBlockY() + "  &7Z:&f" + location.getBlockZ()));
				player.sendMessage(TextUtil.fixColors( "&aUUID: &f" + plot.getUUID()));
				new MessageBuilder(Message.WIDE_HEADER).noPrependedPrefix().send(player);
			} else new MessageBuilder(Message.NULL_PLOT).send(sender);
		}
	}

	@Override
	public List<String> getTabCompletion(Player player, String[] args) {
		return List.of();
	}
}
