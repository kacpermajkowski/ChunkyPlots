package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.*;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.Lang;
import pl.kacpermajkowski.ChunkyPlots.config.Message;
import pl.kacpermajkowski.ChunkyPlots.manager.*;

import java.util.ArrayList;
import java.util.List;

public class PlotDisposeCommand extends Subcommand {

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
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof Player){
			Player player = (Player) sender;

			Chunk chunk = player.getLocation().getChunk();
			String plotID = chunk.getX() + ";" + chunk.getZ();
			Plot plot = PlotManager.getInstance().getPlotByChunk(chunk);
			if(plot != null){
				if(plot.getOwnerNickname().equals(player.getName())) {
					PlotManager.getInstance().disposePlot(plot);
					player.getInventory().addItem(PlotManager.getInstance().getPlotItem());
					player.sendMessage(Config.getInstance().getMessage(Message.PLOT_DELETED).replace("{plotID}", plotID).replace("{world}", plot.getWorldName()));

					User user = UserManager.getInstance().getUser(player.getName());
					for(Group group:user.groups){
						group.plots.remove(plot.getUUID());
					}

					List<VisitPoint> visitPointsToDelete = new ArrayList<>();
					for (VisitPoint visitPoint : VisitManager.getInstance().getVisitPoints()) {
						if (visitPoint.getPlotUUID().equals(plot.getUUID())) visitPointsToDelete.add(visitPoint);
					}
					for (VisitPoint visitPoint : visitPointsToDelete) {
						VisitManager.getInstance().deleteVisitPoint(visitPoint);
						Lang.sendMessage(player, "&cNa usuniętej działce znajdował się punkt &f" + visitPoint.getName() + "&c, więc został on usunięty!");
					}
				} else player.sendMessage(Config.getInstance().getMessage(Message.NOT_OWNER));
			} else player.sendMessage(Config.getInstance().getMessage(Message.NULL_PLOT).replace("{plotID}", plotID));

		} else {
			String message = Config.getInstance().getMessage(Message.SENDER_NOT_PLAYER);
			sender.sendMessage(message);
		}
	}
}
