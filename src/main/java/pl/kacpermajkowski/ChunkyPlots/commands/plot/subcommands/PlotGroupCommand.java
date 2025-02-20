package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.basic.Group;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.basic.User;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.manager.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotGroupCommand extends Subcommand {
	@Override
	public String getName() {
		return "group";
	}

	@Override
	public String getDescription() {
		return "zarządzenie grupami działek";
	}

	@Override
	public String getSyntax() {
		return "/plot group";
	}

	@Override
	public String getPermission() {
		return "chunkyplots.player";
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(args.length == 2) {
				if(args[1].equals("list")){
					sendGroupListToPlayer(player);
				}
			} else if(args.length == 3){
				switch (args[1]) {
					case "create":
						createGroup(player, args[2]);
						break;
					case "delete":
						deleteGroup(player, args[2]);
						break;
					case "add":
						addPlotToGroup(player, args[2], PlotManager.getInstance().getPlotByChunk(player.getLocation().getChunk()));
						break;
					case "remove":
						removePlotFromGroup(player, args[2], PlotManager.getInstance().getPlotByChunk(player.getLocation().getChunk()));
						break;
					case "info":
						sendGroupInfoToPlayer(args[2], player);
						break;
				}
	//			TODO: Add group help message

			} else if(args.length == 6){
				if (args[1].equals("add")) addPlotToGroup(player, args[2], PlotManager.getInstance().getPlotByCoordinates(args[3], args[4], args[5]));
				else if (args[1].equals("remove")) removePlotFromGroup(player, args[2], PlotManager.getInstance().getPlotByCoordinates(args[3], args[4], args[5]));
			}
		}
	}

	private void sendGroupInfoToPlayer(String groupName, Player player) {
		Group group = UserManager.getInstance().getUser(player.getName()).getGroupByName(groupName);
		List<UUID> plotUUIDs = group.plots;
		for(UUID uuid:plotUUIDs){
			player.sendMessage(uuid.toString());
		}
	}

	private void sendGroupListToPlayer(Player player) {
		List<Group> groups = UserManager.getInstance().getUser(player.getName()).getGroups();
		for (Group g: groups){
			player.sendMessage(g.getName());
		}
	}

	private void createGroup(Player player, String groupName){
		User user = UserManager.getInstance().getUser(player.getName());
		if(user != null){
			if(user.getGroupByName(groupName) == null){
				user.groups.add(new Group(groupName));
				UserManager.getInstance().saveUser(user);

				new MessageBuilder(Message.GROUP_CREATE).groupName(groupName).send(player);
			} else {
				new MessageBuilder(Message.GROUP_ALREADY_EXISTS).groupName(groupName).send(player);
			}
		} else {
			new MessageBuilder(Message.NULL_USER).username(player.getName()).send(player);
		}
	}


	private void deleteGroup(Player player, String groupName){
		User user = UserManager.getInstance().getUser(player.getName());
		if(!groupName.equalsIgnoreCase("all")) {
			if (user != null) {
				Group group = user.getGroupByName(groupName);
				if (group != null) {
					user.groups.remove(group);
					UserManager.getInstance().saveUser(user);

					new MessageBuilder(Message.GROUP_DELETE).groupName(groupName).send(player);
				} else {
					new MessageBuilder(Message.NULL_GROUP).groupName(groupName).send(player);
				}
			} else {
				new MessageBuilder(Message.NULL_USER).username(player.getName()).send(player);
			}
		} else {
			new MessageBuilder(Message.CANNOT_DELETE_DEFAULT_GROUP).groupName("all").send(player);
		}
	}


	private void addPlotToGroup(Player player, String groupName, Plot plot){
		User user = UserManager.getInstance().getUser(player.getName());
		if(user != null){
			Group group = user.getGroupByName(groupName);
			if(group != null){
				if(!group.getName().equals("all")) {
					if(!group.plots.contains(plot.getUUID())) {
						group.plots.add(plot.getUUID());
						UserManager.getInstance().saveUser(user);

						new MessageBuilder(Message.PLOT_ADDED_TO_GROUP).groupName(groupName).plot(plot).send(player);
					} else {
						new MessageBuilder(Message.PLOT_ALREADY_IN_GROUP).groupName(groupName).plot(plot).send(player);
					}
				} else {
					new MessageBuilder(Message.CANNOT_ADD_PLOT_TO_DEFAULT_GROUP).groupName("all").plot(plot).send(player);
				}
			} else {
				new MessageBuilder(Message.NULL_GROUP).groupName(groupName).plot(plot).send(player);
			}
		} else {
			new MessageBuilder(Message.NULL_USER).username(player.getName()).send(player);
		}
	}


	private void removePlotFromGroup(Player player, String groupName, Plot plot){
		User user = UserManager.getInstance().getUser(player.getName());
		if(user != null){
			Group group = user.getGroupByName(groupName);
			if(group != null){
				if(!group.getName().equals("all")) {
					if(group.plots.contains(plot.getUUID())){
						group.plots.remove(plot.getUUID());
						UserManager.getInstance().saveUser(user);

						new MessageBuilder(Message.PLOT_REMOVED_FROM_GROUP).group(group).plot(plot).send(player);
					} else {
						new MessageBuilder(Message.PLOT_NOT_IN_GROUP).group(group).plot(plot).send(player);
					}
				} else {
					new MessageBuilder(Message.CANNOT_REMOVE_PLOT_FROM_DEFAULT_GROUP).group(group).plot(plot).send(player);
				}
			} else {
				new MessageBuilder(Message.NULL_GROUP).groupName(groupName).plot(plot).send(player);
			}
		} else {
			new MessageBuilder(Message.NULL_USER).username(player.getName()).send(player);
		}
	}

	public static List<Plot> getPlotsFromGroupName(Player player, String groupName){
		List<Plot> plots = new ArrayList<>();
		User user = UserManager.getInstance().getUser(player.getName());
		Group group = null;
		for(Group g:user.groups){
			if(g.getName().equalsIgnoreCase(groupName)){
				group = g;
				List<UUID> plotUUIDs = group.plots;
				for(UUID plotUUID:plotUUIDs){
					Plot plot = PlotManager.getInstance().getPlotByUUID(plotUUID);
					if(plot != null) plots.add(plot);
					else group.plots.remove(plotUUID);
				}
				return plots;
			}
		}
		new MessageBuilder(Message.NULL_GROUP).groupName(groupName).send(player);
		return plots;

	}
}
