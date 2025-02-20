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
	final PlotManager plotManager = ChunkyPlots.getInstance().plotManager;
	final UserManager userManager = ChunkyPlots.getInstance().userManager;
	final VisitManager visitManager = ChunkyPlots.getInstance().visitManager;

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
			Plot plot = plotManager.getPlotByChunk(chunk);
			if(plot != null){
				if(plot.getOwnerNickname().equals(player.getName())) {
					plotManager.disposePlot(plot);
					player.getInventory().addItem(CraftingManager.plotBlock);
					player.sendMessage(Config.getInstance().getMessage(Message.PLOT_DELETED).replace("{plotID}", plotID).replace("{world}", plot.getWorldName()));

					User user = userManager.getUser(player.getName());
					for(Group group:user.groups){
						group.plots.remove(plot.getUUID());
					}

					List<VisitPoint> visitPointsToDelete = new ArrayList<>();
					for (VisitPoint visitPoint : visitManager.getVisitPoints()) {
						if (visitPoint.getPlotUUID().equals(plot.getUUID())) visitPointsToDelete.add(visitPoint);
					}
					for (VisitPoint visitPoint : visitPointsToDelete) {
						visitManager.deleteVisitPoint(visitPoint);
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
