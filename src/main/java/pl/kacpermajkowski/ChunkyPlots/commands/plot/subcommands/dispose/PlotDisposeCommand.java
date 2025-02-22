package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.dispose;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.basic.*;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.util.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.manager.*;

import java.util.List;

public class PlotDisposeCommand implements PlotSubcommand {

	@Override
	public String getName() {
		return "dispose";
	}

	@Override
	public String getDescription() {
		return "usuwa działkę i zwraca blok działki";
	}

	@Override
	public String getSyntax() {
		return "/plot dispose";
	}

	@Override
	public String getPermission() {
		return "chunkyplots.player";
	}

	@Override
	public void execute(Player sender, String[] args) {
		if(sender instanceof Player){
			Player player = (Player) sender;

			Chunk chunk = player.getLocation().getChunk();
			String plotID = chunk.getX() + ";" + chunk.getZ();
			Plot plot = PlotManager.getInstance().getPlot(chunk);
			if(plot != null){
				if(plot.getOwnerUUID().equals(player.getUniqueId())) {
					PlotManager.getInstance().disposePlot(plot);
					player.getInventory().addItem(PlotManager.getInstance().getPlotItem());
					new MessageBuilder(Message.PLOT_DELETED).plotID(plotID).world(plot.getWorldName()).send(player);

					User user = UserManager.getInstance().getUser(player.getUniqueId());
					for(Group group:user.getGroups()){
						group.remove(plot);
					}

				} else new MessageBuilder(Message.NOT_OWNER).send(player);
			} else new MessageBuilder(Message.NULL_PLOT).plotID(plotID).send(player);
		} else {
			new MessageBuilder(Message.SENDER_NOT_PLAYER).send(sender);
		}
	}

	@Override
	public List<String> getTabCompletion(Player player, String[] args) {
		return List.of();
	}
}
