//package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.blacklist;
//
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import pl.kacpermajkowski.ChunkyPlots.basic.Group;
//import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
//import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
//import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
//import pl.kacpermajkowski.ChunkyPlots.basic.User;
//import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
//import pl.kacpermajkowski.ChunkyPlots.config.lang.MessageBuilder;
//import pl.kacpermajkowski.ChunkyPlots.manager.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class PlotBlacklistCommand implements PlotSubcommand {
//	@Override
//	public String getName() {
//		return "blacklist";
//	}
//
//	@Override
//	public String getDescription() {
//		return "zarządzanie blokowaniem dostępu do dziąłki";
//	}
//
//	@Override
//	public String getSyntax() {
//		return "/plot blacklist";
//	}
//
//	@Override
//	public String getPermission() {
//		return "chunkyplots.blacklist";
//	}
//
//	@Override
//	public void execute(Player sender, String[] args) {
//		if(sender instanceof Player player){
//			if(args.length == 3) {
//				if (args[1].equals("add"))
//					addPlayerToBlacklist(player, args[2], PlotManager.getInstance().getPlot(player.getLocation().getChunk()));
//				else if (args[1].equals("remove"))
//					removePlayerFromBlacklist(player, args[2], PlotManager.getInstance().getPlot(player.getLocation().getChunk()));
////			TODO: Add help for blacklist command
//			} else if (args.length == 4){
////				TODO: Fix player not exist message appearing more than one time due to multiple plots
////				TODO: Fix being able to blacklist a member
////				TODO: Fix player unable to move after being blacklisted
////				TODO: Fix being able to add one player to blacklist multiple times
//				if (args[1].equals("add")) {
//					List<Plot> plots = getPlotsFromGroupName(player, args[3]);
//					if(!plots.isEmpty()) for(Plot plot: plots) addPlayerToBlacklist(player, args[2], plot );
//					else new MessageBuilder(Message.EMPTY_GROUP).send(sender);
//
//				}
//				else if (args[1].equals("remove")) {
//					List<Plot> plots = getPlotsFromGroupName(player, args[3]);
//					if(!plots.isEmpty()) for(Plot plot: plots) removePlayerFromBlacklist(player, args[2], plot);
//					else new MessageBuilder(Message.EMPTY_GROUP).send(sender);
//				}
//			} else if(args.length == 6){
//				if (args[1].equals("add")) addPlayerToBlacklist(player, args[2], PlotManager.getInstance().getPlot(args[3], args[4], args[5]));
//				else if (args[1].equals("remove")) removePlayerFromBlacklist(player, args[2], PlotManager.getInstance().getPlot(args[3], args[4], args[5]));
//			}
//		}
//	}
//
//	private void addPlayerToBlacklist(Player player, String userName, Plot plot){
//		if(plot != null) {
//			if(plot.getOwnerNickname().equals(player.getName())){
//				if(!userName.equals(plot.getOwnerNickname())){
//					User user = UserManager.getInstance().getUser(userName);
//					if(user != null){
//						plot.getBlacklist().add(userName);
//						PlotManager.getInstance().savePlot(plot);
//						new MessageBuilder(Message.BLACKLIST_ADDED_TO_PLOT).plot(plot).username(userName).send(player);
//					} else {
//						new MessageBuilder(Message.NULL_USER).username(userName).send(player);
//					}
//				} else {
//					new MessageBuilder(Message.CANNOT_ADD_OWNER_TO_BLACKLIST).send(player);
//				}
//			} else {
//				new MessageBuilder(Message.NOT_OWNER).send(player);
//			}
//		} else {
//			new MessageBuilder(Message.NULL_PLOT).send(player);
//		}
//	}
//
//
//	private void removePlayerFromBlacklist(Player player, String userName, Plot plot){
//		if(plot != null) {
//			if(plot.getOwnerNickname().equals(player.getName())){
//				if(plot.getBlacklist().contains(userName)){
//					plot.getBlacklist().remove(userName);
//					PlotManager.getInstance().savePlot(plot);
//
//					new MessageBuilder(Message.BLACKLIST_REMOVED_FROM_PLOT).plot(plot).username(userName).send(player);
//				} else {
//					new MessageBuilder(Message.NULL_USER).username(userName).send(player);
//				}
//			} else {
//				new MessageBuilder(Message.NOT_OWNER).send(player);
//			}
//		} else {
//			new MessageBuilder(Message.NULL_PLOT).send(player);
//		}
//	}
//
//	private List<Plot> getPlotsFromGroupName(Player player, String groupName){
//		List<Plot> plots = new ArrayList<>();
//		User user = UserManager.getInstance().getUser(player.getUniqueId());
//		Group group = null;
//		for(Group g:user.getGroups()){
//			if(g.getName().equalsIgnoreCase(groupName)){
//				group = g;
//				List<UUID> plotUUIDs = group.plots;
//				for(UUID plotUUID:plotUUIDs){
//					Plot plot = PlotManager.getInstance().getPlot(plotUUID);
//					if(plot != null) plots.add(plot);
//					else group.plots.remove(plotUUID);
//				}
//				return plots;
//			}
//		}
//		new MessageBuilder(Message.NULL_GROUP).send(player);
//		return plots;
//
//	}
//
//
//	@Override
//	public List<String> getTabCompletion(CommandSender sender, String[] args) {
//		return List.of();
//	}
//}
//
