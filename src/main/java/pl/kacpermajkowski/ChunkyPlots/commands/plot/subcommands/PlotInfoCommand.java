package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.TextUtil;

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
				player.sendMessage(TextUtil.fixColors( "&9-----------{ " + Config.getInstance().getPrefix() + " &9}-----------"));
				player.sendMessage(TextUtil.fixColors( "&aWłaściciel: &f" + plot.getOwnerNickname()));
				player.sendMessage(TextUtil.fixColors( "&aChunk X: &f" + plot.getChunkX()));
				player.sendMessage(TextUtil.fixColors( "&aChunk Z: &f" + plot.getChunkZ()));
				player.sendMessage(TextUtil.fixColors( "&aŚwiat: &f" + plot.getWorldName()));
				player.sendMessage(TextUtil.fixColors( "&aCzłonkowie: &f" + plot.members.toString()));
				player.sendMessage(TextUtil.fixColors( "&aZablokowani: &f" + plot.blacklist.toString()));
				Location location = player.getWorld().getChunkAt(plot.getChunkX(), plot.getChunkZ()).getBlock(8, 64, 8).getLocation();
				player.sendMessage(TextUtil.fixColors( "&aLokalizacja: &7X:&f" + location.getBlockX() + "  &7Y:&f" + location.getBlockY() + "  &7Z:&f" + location.getBlockZ()));
				player.sendMessage(TextUtil.fixColors( "&aUUID: &f" + plot.getUUID()));
				player.sendMessage(TextUtil.fixColors( "&9-----------{ " + Config.getInstance().getPrefix() + " &9}-----------"));
			} else player.sendMessage(TextUtil.fixColors(Config.getInstance().getMessage(Message.NULL_PLOT)));
		}
	}
}
