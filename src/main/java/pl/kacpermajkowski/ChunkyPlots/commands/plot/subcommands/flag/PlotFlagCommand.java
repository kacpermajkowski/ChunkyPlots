//package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.flag;
//
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import pl.kacpermajkowski.ChunkyPlots.basic.Flag;
//import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
//import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
//import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
//import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
//import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotCommand;
//import pl.kacpermajkowski.ChunkyPlots.config.Config;
//import pl.kacpermajkowski.ChunkyPlots.config.lang.MessageBuilder;
//import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;
//import pl.kacpermajkowski.ChunkyPlots.util.TextUtil;
//
//import java.util.HashMap;
//import java.util.List;
//
//import static pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.group.PlotGroupCommand.getPlotsFromGroupName;
//
//public class PlotFlagCommand implements PlotSubcommand {
//
//	@Override
//	public String getName() {
//		return "flag";
//	}
//
//	@Override
//	public String getDescription() {
//		return "zarządzanie flagami działki";
//	}
//
//	@Override
//	public String getSyntax() {
//		return "/plot flag";
//	}
//
//	@Override
//	public String getPermission() {
//		return "chunkyplots.flag";
//	}
//
//	@Override
//	public void execute(Player sender, String[] args) {
//		if(sender instanceof Player player){
//			if(args.length == 2) {
//				if (args[1].equals("defaults")) displayFlags(player, true, null);
//				else if (args[1].equals("list"))
//					displayFlags(player, false, PlotManager.getInstance().getPlot(player.getLocation().getChunk()));
//				else PlotCommand.getHelpSubcommand().execute(sender, args);
//			} else if(args.length == 3){
//				if(args[1].equals("check")) displayFlagValue(player, args[2], PlotManager.getInstance().getPlot(player.getLocation().getChunk()));
//			} else if(args.length == 4){
////				TODO: Fix concurrent modification exception
//				if (args[1].equals("check")) {
//					List<Plot> plots = getPlotsFromGroupName(player, args[3]);
//					if(!plots.isEmpty()) {
//						boolean isValueSameInAllPlots = true;
//						Flag flag = Flag.getByName(args[2]);
//						if(flag != null) {
//							boolean value = plots.getFirst().getFlags().get(flag);
//							for (Plot plot : plots) {
//								if(!value == plot.getFlags().get(flag)){
//									isValueSameInAllPlots = false;
//									break;
//								}
//							}
//							if(isValueSameInAllPlots){
//								new MessageBuilder(Message.FLAG_VALUE_ON_PLOT).flag(flag, value);
//							} else {
//								new MessageBuilder(Message.FLAG_VALUES_IN_GROUP_ARE_DIFFERENT).flag(flag).send(player);
//							}
//						}
//					}
//					else new MessageBuilder(Message.EMPTY_GROUP).send(player);
//				}
//				else if (args[1].equals("set")) setFlagValue(player, args[2], args[3], PlotManager.getInstance().getPlot(player.getLocation().getChunk()));
//			} else if(args.length == 5){
//				if (args[1].equals("set")){
//					List<Plot> plots = getPlotsFromGroupName(player, args[4]);
//					if(!plots.isEmpty()) for(Plot plot: plots) setFlagValue(player, args[2], args[3], plot);
//					else new MessageBuilder(Message.EMPTY_GROUP).send(player);
//				}
//				else if(args[1].equals("list")) displayFlags(player, false, PlotManager.getInstance().getPlot(args[2], args[3], args[4]));
////				TODO: flag help message
//			}
//		}
//	}
//
//	private void displayFlagValue(Player player, String flagName, Plot plot) {
//		if(plot != null){
////			TODO: message manager flag value
//			Flag flag = Flag.getByName(flagName);
//			if(flag != null){
//				boolean flagValue = plot.getFlags().get(flag);
//
//				new MessageBuilder(Message.FLAG_VALUE_ON_PLOT).flag(flag, flagValue).plot(plot).send(player);
//			} else {
//				new MessageBuilder(Message.NULL_FLAG).flagName(flagName).send(player);
//			}
//		} else {
//			new MessageBuilder(Message.NULL_PLOT).send(player);
//		}
//	}
//
//
//
//	private void displayFlags(Player player, boolean defaultOrNot, Plot plot) {
//		HashMap<Flag, Boolean> flags;
//		if(defaultOrNot) {
//			flags = Config.getInstance().getDefaultFlags();
//			new MessageBuilder(Message.DEFAULT_FLAGS).noPrependedPrefix().send(player);
//		} else {
//			if(plot != null){
//				flags = plot.getFlags();
//				new MessageBuilder(Message.PLOT_FLAGS).noPrependedPrefix().plot(plot).send(player);
//			} else {
//				player.sendMessage(Config.getInstance().getMessage(Message.NULL_PLOT));
//				new MessageBuilder(Message.NULL_PLOT).send(player);
//				return;
//			}
//		}
//		if (flags != null) {
//			for(Flag flag:flags.keySet()){
//				boolean flagValue = flags.get(flag);
//				//TODO: Do it properly in the config when the time comes to refactor commands
//				player.sendMessage(TextUtil.fixColors("&7" + flag.toString() + "&8: " + (flagValue ? "&atrue" : "&cfalse")));
//			}
//		} else {
//			new MessageBuilder(Message.ERROR_UNSPECIFIED).send(player);
//		}
//	}
//
//	private void setFlagValue(Player player, String flagName, String flagValue, Plot plot) {
//		if(plot != null){
//			if(plot.isPlayerOwner(player)){
//				Flag flag = Flag.getByName(flagName.toUpperCase());
////				TODO: Proper checking if flag exists before taking value of it
//				if(flag != null){
//					if(flagValue.equalsIgnoreCase("true") || flagValue.equalsIgnoreCase("false")){
//						boolean value = Boolean.parseBoolean(flagValue);
//						plot.setFlag(flag, value);
//						PlotManager.getInstance().savePlot(plot);
//
//						new MessageBuilder(Message.FLAG_SET_ON_PLOT).plot(plot).flag(flag, value).send(player);
//					} else {
//						new MessageBuilder(Message.WRONG_FLAG_VALUE).flagValue(flagValue).send(player);
//					}
//				} else {
//					new MessageBuilder(Message.NULL_FLAG).flagName(flagName).send(player);
//				}
//			} else {
//				new MessageBuilder(Message.NOT_OWNER).send(player);
//			}
//		} else {
//			new MessageBuilder(Message.NULL_PLOT).send(player);
//		}
//	}
//
//	@Override
//	public List<String> getTabCompletion(CommandSender sender, String[] args) {
//		return List.of();
//	}
//
//}
