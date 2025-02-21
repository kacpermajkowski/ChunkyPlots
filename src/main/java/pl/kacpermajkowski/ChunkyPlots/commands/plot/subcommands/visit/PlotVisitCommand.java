package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.visit;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.basic.User;
import pl.kacpermajkowski.ChunkyPlots.basic.VisitPoint;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.manager.*;
import pl.kacpermajkowski.ChunkyPlots.util.TextUtil;

import java.util.List;

public class PlotVisitCommand implements PlotSubcommand {
	@Override
	public String getName() {
		return "visit";
	}

	@Override
	public String getDescription() {
		return "tworzenie i odwiedzanie punktów wizyt";
	}

	@Override
	public String getSyntax() {
		return "/plot visit";
	}

	@Override
	public String getPermission() {
		return "chunkyplots.visit";
	}

	@Override
	public void execute(Player sender, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;

			if(args.length == 2) {
				String visitPointName = args[1];
				final VisitPoint visitPoint = VisitManager.getInstance().getVisitPoint(visitPointName);
				if (visitPoint != null) {
					if (visitPoint.isOpen) {
						if (visitPoint.isSafe()) {
							final User user = UserManager.getInstance().getUser(player);
							if (!user.isTeleporting()) {
								user.setTeleporting(true);
								String rawMessage = Config.getInstance().getMessage(Message.TELEPORTING_TO_VISIT_POINT);
								String uncolouredMessage = TextUtil.replacePlaceholders(rawMessage, visitPoint);
								TextUtil.sendMessage(player, uncolouredMessage);
								final int x = player.getLocation().getBlockX();
								final int y = player.getLocation().getBlockY();
								final int z = player.getLocation().getBlockZ();
								ChunkyPlots.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(ChunkyPlots.getInstance(), () -> {
									if (player.isOnline()) {
										if (player.getLocation().getBlockX() == x && player.getLocation().getBlockY() == y && player.getLocation().getBlockZ() == z) {
											player.teleport(visitPoint.getLocation());
											String rawMessage1 = Config.getInstance().getMessage(Message.TELEPORTED_TO_VISIT_POINT);
											String uncolouredMessage1 = TextUtil.replacePlaceholders(rawMessage1, visitPoint);
											TextUtil.sendMessage(player, uncolouredMessage1);
										} else {
											String rawMessage1 = Config.getInstance().getMessage(Message.TELEPORT_CANCELLED);
											String uncolouredMessage1 = TextUtil.replacePlaceholders(rawMessage1, visitPoint);
											TextUtil.sendMessage(player, uncolouredMessage1);
										}
									}
									user.setTeleporting(false);
								}, 20 * 5);
							} else {
								String rawMessage = Config.getInstance().getMessage(Message.ALREADY_TELEPORTING);
								TextUtil.sendMessage(player, rawMessage);
							}
						} else {
							String rawMessage = Config.getInstance().getMessage(Message.VISIT_POINT_NOT_SAFE).replace("{visitPointName}", visitPointName);
							TextUtil.sendMessage(player, rawMessage);
						}
					} else {
						String rawMessage = Config.getInstance().getMessage(Message.VISIT_POINT_CLOSED).replace("{visitPointName}", visitPointName);
						String uncolouredMessage = TextUtil.replacePlaceholders(rawMessage, visitPoint);
						TextUtil.sendMessage(player, uncolouredMessage);
					}
				} else {
					String rawMessage = Config.getInstance().getMessage(Message.NULL_VISIT_POINT).replace("{visitPointName}", visitPointName);
					TextUtil.sendMessage(player, rawMessage);
				}
			} else if (args.length == 3){
				if(args[1].equals("createpoint")) createVisitPoint(player, args[2], null, PlotManager.getInstance().getPlot(player.getLocation().getChunk()));
				else if(args[1].equals("deletepoint")) deleteVisitPoint(player, args[2]);
//				TODO: Add visit help command
			}
		}
	}

	@Override
	public List<String> getTabCompletion(CommandSender sender, String[] args) {
		return List.of();
	}

	private void createVisitPoint(Player player, String name, String description, Plot plot){
		if(plot != null) {
			for (VisitPoint visitPoint : VisitManager.getInstance().getVisitPoints()) {
				if (visitPoint.getName().equals(name)) {
					String rawMessage = Config.getInstance().getMessage(Message.VISIT_POINT_ALREADY_EXISTS);
					String uncolouredMessage = TextUtil.replacePlaceholders(rawMessage, visitPoint);
					TextUtil.sendMessage(player, uncolouredMessage);
					return;
				}
			}
			Location location = player.getLocation();
			VisitPoint visitPoint = new VisitPoint(location, plot.getUUID(), player.getName(), name, description);
			VisitManager.getInstance().createVisitPoint(visitPoint);

			String rawMessage = Config.getInstance().getMessage(Message.CREATED_VISIT_POINT);
			String uncolouredMessage = TextUtil.replacePlaceholders(rawMessage, visitPoint);
			TextUtil.sendMessage(player, uncolouredMessage);
		} else {
			String rawMessage = Config.getInstance().getMessage(Message.VISIT_POINT_NOT_INSIDE_PLOT);
			TextUtil.sendMessage(player, rawMessage);
		}
	}

	private void deleteVisitPoint(Player player, String visitPointName){
		VisitPoint visitPoint = VisitManager.getInstance().getVisitPoint(visitPointName);
		if(visitPoint != null) {
			if(visitPoint.getOwnerName().equals(player.getName())){
				VisitManager.getInstance().deleteVisitPoint(visitPoint);

				String rawMessage = Config.getInstance().getMessage(Message.DELETED_VISIT_POINT);
				String uncolouredMessage = TextUtil.replacePlaceholders(rawMessage, visitPoint);
				TextUtil.sendMessage(player, uncolouredMessage);
			} else {
				String rawMessage = Config.getInstance().getMessage(Message.NOT_VISIT_POINT_OWNER);
				String uncolouredMessage = TextUtil.replacePlaceholders(rawMessage, visitPoint, player);
				TextUtil.sendMessage(player, uncolouredMessage);
			}
		} else {
			String rawMessage = Config.getInstance().getMessage(Message.NULL_VISIT_POINT);
			String uncolouredMessage = rawMessage.replace("{visitPointName}", visitPointName);
			TextUtil.sendMessage(player, uncolouredMessage);
		}
	}
}
