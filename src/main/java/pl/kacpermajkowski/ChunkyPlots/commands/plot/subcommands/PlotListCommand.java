package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.util.TextUtil;

import java.util.List;

public class PlotListCommand implements Subcommand {
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
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof Player player){
			player.sendMessage("");
			player.sendMessage("");
			TextUtil.sendNoPrefixMessage(player, "&eID Działki &9» &ePrzybliżone koordynaty działki");
			for(Plot plot:PlotManager.getInstance().getPlots()){
				if(plot.getOwnerNickname().equals(player.getName())) {
					Location location = player.getWorld().getChunkAt(plot.getChunkX(), plot.getChunkZ()).getBlock(8, 64, 8).getLocation();
					TextUtil.sendNoPrefixMessage(player,"&8(&6" + plot.getID() + "&8)" + " &a» &7X:&f" + location.getBlockX() + "  &7Y:&f" + location.getBlockY() + "  &7Z:&f" + location.getBlockZ());
				}
			}
			player.sendMessage("");
			player.sendMessage("");
		}
	}

	@Override
	public List<String> getTabCompletion(CommandSender sender, String[] args) {
		return List.of();
	}
}
