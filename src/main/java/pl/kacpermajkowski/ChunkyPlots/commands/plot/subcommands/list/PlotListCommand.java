package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.list;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;

import java.util.List;

public class PlotListCommand implements PlotSubcommand {
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
			new MessageBuilder(Message.WIDE_HEADER).send(player);
			TextUtil.sendNoPrefixMessage(player, "&eID Działki &9» &ePrzybliżone koordynaty działki");
			for(Plot plot:PlotManager.getInstance().getPlots()){
				if(plot.isPlayerOwner(player)) {
					Location location = player.getWorld().getChunkAt(plot.getChunkX(), plot.getChunkZ()).getBlock(8, 64, 8).getLocation();
					TextUtil.sendNoPrefixMessage(player,"&8(&6" + plot.getID() + "&8)" + " &a» &7X:&f" + location.getBlockX() + "  &7Y:&f" + location.getBlockY() + "  &7Z:&f" + location.getBlockZ());
				}
			}
			new MessageBuilder(Message.WIDE_HEADER).send(player);
		}
	}

	@Override
	public List<String> getTabCompletion(Player player, String[] args) {
		return List.of();
	}
}
