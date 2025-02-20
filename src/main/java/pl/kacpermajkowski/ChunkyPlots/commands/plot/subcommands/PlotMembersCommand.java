package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.config.Message;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.basic.User;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.Lang;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.manager.UserManager;

import java.util.List;

public class PlotMembersCommand extends Subcommand {
	@Override
	public String getName() {
		return "members";
	}

	@Override
	public String getDescription() {
		return "zarządzanie członkami działki";
	}

	@Override
	public String getSyntax() {
		return "/plot members";
	}

	@Override
	public String getPermission() {
		return "chunkyplots.members";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof Player player) {
			if(args.length == 3) {
					if (args[1].equals("add"))
						addMemberToPlot(player, args[2], PlotManager.getInstance().getPlotByChunk(player.getLocation().getChunk()));
					else if (args[1].equals("remove"))
						removeMemberFromPlot(player, args[2], PlotManager.getInstance().getPlotByChunk(player.getLocation().getChunk()));
	//			TODO: add members help command
			} else if(args.length == 4){
				if (args[1].equals("add")) {
					List<Plot> plots = PlotGroupCommand.getPlotsFromGroupName(player, args[3]);
					if(plots.size() > 0) for (Plot plot : plots) addMemberToPlot(player, args[2], plot);
					else Lang.sendMessage(player, "&cDo tej grupy nie są przypisane żadne działki");
				}
				else if (args[1].equals("remove")) {
					List<Plot> plots = PlotGroupCommand.getPlotsFromGroupName(player, args[3]);
					if(plots.size() > 0) for (Plot plot : plots) removeMemberFromPlot(player, args[2], plot);
					else Lang.sendMessage(player, "&cDo tej grupy nie są przypisane żadne działki");
				}
			} else if(args.length == 6){
				if (args[1].equals("add")) addMemberToPlot(player, args[2], PlotManager.getInstance().getPlotByCoordinates(args[3], args[4], args[5]));
				else if (args[1].equals("remove"))removeMemberFromPlot(player, args[2], PlotManager.getInstance().getPlotByCoordinates(args[3], args[4], args[5]));
			}
		}
	}

	private void addMemberToPlot(Player player, String userName, Plot plot) {
		if (plot != null) {
			if (plot.getOwnerNickname().equals(player.getName())) {
				if (!userName.equals(plot.getOwnerNickname())) {
					User user = UserManager.getInstance().getUser(userName);
					if (user != null) {
						if(!plot.members.contains(userName)) {
							plot.members.add(userName);
							PlotManager.getInstance().savePlot(plot);
							String rawMessage = Config.getInstance().getMessage(Message.ADDED_MEMBER_TO_PLOT);
							String uncolouredMessage = Lang.replacePlaceholders(rawMessage, plot, user);
							Lang.sendMessage(player, uncolouredMessage);
						} else {
							String rawMessage = Config.getInstance().getMessage(Message.PLAYER_IS_ALREADY_A_MEMBER);
							String uncolouredMessage = Lang.replacePlaceholders(rawMessage, plot, user);
							Lang.sendMessage(player, uncolouredMessage);
						}
					} else {
						String rawMessage = Config.getInstance().getMessage(Message.NULL_USER).replace("{userName}", userName);
						Lang.sendMessage(player, rawMessage);
					}
				} else {
					String rawMessage = Config.getInstance().getMessage(Message.CANNOT_ADD_OWNER_AS_MEMBER);
					String uncolouredMessage = Lang.replacePlaceholders(rawMessage, player);
					Lang.sendMessage(player, uncolouredMessage);
				}
			} else {
				String rawMessage = Config.getInstance().getMessage(Message.NOT_OWNER);
				String uncolouredMessage = Lang.replacePlaceholders(rawMessage, plot, player);
				Lang.sendMessage(player, uncolouredMessage);
			}
		} else {
			String rawMessage = Config.getInstance().getMessage(Message.NULL_PLOT);
			Lang.sendMessage(player, rawMessage);
		}
	}


	private void removeMemberFromPlot(Player player, String userName, Plot plot){
		if(plot != null) {
			if(plot.getOwnerNickname().equals(player.getName())){
				if(plot.members.contains(userName)){
					plot.members.remove(userName);
					PlotManager.getInstance().savePlot(plot);

					User user = UserManager.getInstance().getUser(userName);
					String rawMessage = Config.getInstance().getMessage(Message.REMOVED_MEMBER_FROM_PLOT);
					String uncolouredMessage = Lang.replacePlaceholders(rawMessage, plot, user);
					Lang.sendMessage(player, uncolouredMessage);
				} else {
					String rawMessage = Config.getInstance().getMessage(Message.NULL_USER).replace("{userName}", userName);
					Lang.sendMessage(player, rawMessage);
				}
			} else {
				String rawMessage = Config.getInstance().getMessage(Message.NOT_OWNER);
				String uncolouredMessage = Lang.replacePlaceholders(rawMessage, plot, player);
				Lang.sendMessage(player, uncolouredMessage);
			}
		} else {
			String rawMessage = Config.getInstance().getMessage(Message.NULL_PLOT);
			Lang.sendMessage(player, rawMessage);
		}
	}
}
