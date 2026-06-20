package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.dispose;

import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.group.Group;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.utils.ActionResult;
import pl.kacpermajkowski.ChunkyPlots.utils.PlayerUtil;

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
	public void execute(Player player, String[] args) {
		Plot plot = PlotManager.getInstance().getPlot(player);

		ActionResult result = tryDisposePlot(player, plot);
		new MessageBuilder(result.getResultMessage())
				.plot(plot)
				.world(plot.getWorldName())
				.sendChat(player);
		if(!result.isSuccess()){
			return;
		}

		refundPlotBlock(player);

		User user = UserManager.getInstance().getUser(player);
		removePlotFromGroups(user, plot);

		MessageBuilder mb = new MessageBuilder(Message.CURRENT_PLOT_DELETED).plot(plot);
		PlayerUtil.getPlayersInPlot(plot).forEach(mb::sendChat);
		PlayerUtil.getPlayersInPlot(plot).forEach(p -> {
			UserManager.getInstance().getUser(p).setCachedCurrentPlot(null);
		});
	}

	private void refundPlotBlock(Player player){
		player.getInventory().addItem(PlotManager.getInstance().getPlotItem());
	}

	private void removePlotFromGroups(User user, Plot plot){
		for(Group group: user.getGroups()){
			group.remove(plot);
			if(!group.isDefault()){
				new MessageBuilder(Message.PLOT_REMOVED_FROM_GROUP)
						.group(group)
						.sendChat(user.getPlayer());
			}
		}
	}

	private ActionResult tryDisposePlot(Player player, Plot plot){
		if(plot == null)
			return new ActionResult(false, Message.NULL_PLOT);
		if(!plot.isPlayerOwner(player))
			return new ActionResult(false, Message.NOT_OWNER);

		PlotManager.getInstance().disposePlot(plot);
		return new ActionResult(true, Message.PLOT_DELETED);
	}

	@Override
	public List<String> getTabCompletion(Player player, String[] args) {
		return List.of();
	}
}
