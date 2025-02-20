package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.config.Message;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.Lang;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;

public class PlotInfoCommand extends Subcommand {
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
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof Player player) {
 			Plot plot = PlotManager.getInstance().getPlotByChunk(player.getLocation().getChunk());
			if (plot != null) {
				Lang.sendMessage(player, "&9-----------{ " + Config.getInstance().getPrefix() + " &9}-----------");
				Lang.sendMessage(player, "&aWłaściciel: &f" + plot.getOwnerNickname());
				Lang.sendMessage(player, "&aChunk X: &f" + plot.getChunkX());
				Lang.sendMessage(player, "&aChunk Z: &f" + plot.getChunkZ());
				Lang.sendMessage(player, "&aŚwiat: &f" + plot.getWorldName());
				Lang.sendMessage(player, "&aCzłonkowie: &f" + plot.members.toString());
				Lang.sendMessage(player, "&aZablokowani: &f" + plot.blacklist.toString());
				Location location = player.getWorld().getChunkAt(plot.getChunkX(), plot.getChunkZ()).getBlock(8, 64, 8).getLocation();
				Lang.sendMessage(player, "&aLokalizacja: &7X:&f" + location.getBlockX() + "  &7Y:&f" + location.getBlockY() + "  &7Z:&f" + location.getBlockZ());
				Lang.sendMessage(player, "&aUUID: &f" + plot.getUUID());
				Lang.sendMessage(player, "&9-----------{ " + Config.getInstance().getPrefix() + " &9}-----------");
			} else player.sendMessage(Config.getInstance().getMessage(Message.NULL_PLOT));
		}
	}
}
