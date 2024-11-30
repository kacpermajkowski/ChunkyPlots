package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.MessageType;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.basic.User;
import pl.kacpermajkowski.ChunkyPlots.basic.VisitPoint;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.manager.*;

public class PlotVisitCommand extends Subcommand {
	final ConfigManager configManager = ChunkyPlots.plugin.configManager;
	final PlotManager plotManager = ChunkyPlots.plugin.plotManager;
	final UserManager userManager = ChunkyPlots.plugin.userManager;
	final VisitManager visitManager = ChunkyPlots.plugin.visitManager;
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
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;

			if(args.length == 2) {
				String visitPointName = args[1];
				final VisitPoint visitPoint = visitManager.getVisitPoint(visitPointName);
				if (visitPoint != null) {
					if (visitPoint.isOpen) {
						if (visitPoint.isSafe()) {
							final User user = userManager.getUser(player.getName());
							if (!user.isTeleporting) {
								user.isTeleporting = true;
								String rawMessage = configManager.getMessage(MessageType.TELEPORTING_TO_VISIT_POINT);
								String uncolouredMessage = MessageManager.replacePlaceholders(rawMessage, visitPoint);
								MessageManager.sendMessage(player, uncolouredMessage);
								final int x = player.getLocation().getBlockX();
								final int y = player.getLocation().getBlockY();
								final int z = player.getLocation().getBlockZ();
								ChunkyPlots.plugin.getServer().getScheduler().scheduleSyncDelayedTask(ChunkyPlots.plugin, () -> {
									if (player.isOnline()) {
										if (player.getLocation().getBlockX() == x && player.getLocation().getBlockY() == y && player.getLocation().getBlockZ() == z) {
											player.teleport(visitPoint.getLocation());
											String rawMessage1 = configManager.getMessage(MessageType.TELEPORTED_TO_VISIT_POINT);
											String uncolouredMessage1 = MessageManager.replacePlaceholders(rawMessage1, visitPoint);
											MessageManager.sendMessage(player, uncolouredMessage1);
										} else {
											String rawMessage1 = configManager.getMessage(MessageType.TELEPORT_CANCELLED);
											String uncolouredMessage1 = MessageManager.replacePlaceholders(rawMessage1, visitPoint);
											MessageManager.sendMessage(player, uncolouredMessage1);
										}
									}
									user.isTeleporting = false;
								}, 20 * 5);
							} else {
								String rawMessage = configManager.getMessage(MessageType.ALREADY_TELEPORTING);
								MessageManager.sendMessage(player, rawMessage);
							}
						} else {
							String rawMessage = configManager.getMessage(MessageType.VISIT_POINT_NOT_SAFE).replace("{visitPointName}", visitPointName);
							MessageManager.sendMessage(player, rawMessage);
						}
					} else {
						String rawMessage = configManager.getMessage(MessageType.VISIT_POINT_CLOSED).replace("{visitPointName}", visitPointName);
						String uncolouredMessage = MessageManager.replacePlaceholders(rawMessage, visitPoint);
						MessageManager.sendMessage(player, uncolouredMessage);
					}
				} else {
					String rawMessage = configManager.getMessage(MessageType.NULL_VISIT_POINT).replace("{visitPointName}", visitPointName);
					MessageManager.sendMessage(player, rawMessage);
				}
			} else if (args.length == 3){
				if(args[1].equals("createpoint")) createVisitPoint(player, args[2], null, plotManager.getPlotByChunk(player.getLocation().getChunk()));
				else if(args[1].equals("deletepoint")) deleteVisitPoint(player, args[2]);
//				TODO: Add visit help command
			}
		}
	}
	private void createVisitPoint(Player player, String name, String description, Plot plot){
		if(plot != null) {
			for (VisitPoint visitPoint : visitManager.getVisitPoints()) {
				if (visitPoint.getName().equals(name)) {
					String rawMessage = configManager.getMessage(MessageType.VISIT_POINT_ALREADY_EXISTS);
					String uncolouredMessage = MessageManager.replacePlaceholders(rawMessage, visitPoint);
					MessageManager.sendMessage(player, uncolouredMessage);
					return;
				}
			}
			Location location = player.getLocation();
			VisitPoint visitPoint = new VisitPoint(location, plot.getUUID(), player.getName(), name, description);
			visitManager.createVisitPoint(visitPoint);

			String rawMessage = configManager.getMessage(MessageType.CREATED_VISIT_POINT);
			String uncolouredMessage = MessageManager.replacePlaceholders(rawMessage, visitPoint);
			MessageManager.sendMessage(player, uncolouredMessage);
		} else {
			String rawMessage = configManager.getMessage(MessageType.VISIT_POINT_NOT_INSIDE_PLOT);
			MessageManager.sendMessage(player, rawMessage);
		}
	}

	private void deleteVisitPoint(Player player, String visitPointName){
		VisitPoint visitPoint = visitManager.getVisitPoint(visitPointName);
		if(visitPoint != null) {
			if(visitPoint.getOwnerName().equals(player.getName())){
				visitManager.deleteVisitPoint(visitPoint);

				String rawMessage = configManager.getMessage(MessageType.DELETED_VISIT_POINT);
				String uncolouredMessage = MessageManager.replacePlaceholders(rawMessage, visitPoint);
				MessageManager.sendMessage(player, uncolouredMessage);
			} else {
				String rawMessage = configManager.getMessage(MessageType.NOT_VISIT_POINT_OWNER);
				String uncolouredMessage = MessageManager.replacePlaceholders(rawMessage, visitPoint, player);
				MessageManager.sendMessage(player, uncolouredMessage);
			}
		} else {
			String rawMessage = configManager.getMessage(MessageType.NULL_VISIT_POINT);
			String uncolouredMessage = rawMessage.replace("{visitPointName}", visitPointName);
			MessageManager.sendMessage(player, uncolouredMessage);
		}
	}
}
